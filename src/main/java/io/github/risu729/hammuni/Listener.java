/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import com.vdurmont.emoji.EmojiManager;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static io.github.risu729.hammuni.EventFilter.shouldIgnore;
import static io.github.risu729.hammuni.PointEvent.ATTACHMENT;
import static io.github.risu729.hammuni.PointEvent.ATTACHMENT_SHARE;
import static io.github.risu729.hammuni.PointEvent.EMOJI;
import static io.github.risu729.hammuni.PointEvent.FIRST_MESSAGE;
import static io.github.risu729.hammuni.PointEvent.MESSAGE;
import static io.github.risu729.hammuni.PointEvent.PRIVATE_THREAD_CREATE;
import static io.github.risu729.hammuni.PointEvent.PUBLIC_THREAD_CREATE;
import static io.github.risu729.hammuni.PointEvent.REACTION;
import static io.github.risu729.hammuni.PointEvent.SURVEY_CHANNEL_REACTION;
import static io.github.risu729.hammuni.PointEvent.SURVEY_MESSAGE;
import static io.github.risu729.hammuni.PointEvent.SURVEY_MESSAGE_REACTION;
import static io.github.risu729.hammuni.PointEvent.URL;
import static io.github.risu729.hammuni.PointEvent.URL_SHARE;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Listener extends ListenerAdapter {

  private static final Pattern URL_PATTERN = Pattern.compile(
      "(?:https?|ftp)://[\\w/:%#$&?()~.=+\\-]+");
  private static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile(":[A-Za-z0-9_]{2,}:[0-9]+");

  @NotNull List<@NotNull String> surveyChannels;
  @NotNull List<@NotNull String> sharingChannels;
  @NotNull PointApi pointApi;

  @NotNull Map<String, Integer> messageCounts = new HashMap<>();

  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  Listener(@NotNull PointApi pointApi,
      @Value("${hammuni.bot.survey-channels}") @NotNull List<@NotNull String> surveyChannels,
      @Value("${hammuni.bot.sharing-channels}") @NotNull List<@NotNull String> sharingChannels) {
    this.pointApi = pointApi;

    this.surveyChannels = surveyChannels;
    this.sharingChannels = sharingChannels;
  }


  @SuppressWarnings("OverlyComplexMethod")
  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    var user = event.getAuthor();
    if (shouldIgnore(event.getGuild(), user)) {
      return;
    }

    var pointEvents = EnumSet.noneOf(PointEvent.class);

    if (!event.getMessage().getAttachments().isEmpty()) {
      pointEvents.add(ATTACHMENT);
      if (isSharingChannel(event.getChannel())) {
        pointEvents.add(ATTACHMENT_SHARE);
      }
    }

    var contentRaw = event.getMessage().getContentRaw();
    if (URL_PATTERN.matcher(contentRaw).find()) {
      pointEvents.add(URL);
      if (isSharingChannel(event.getChannel())) {
        pointEvents.add(URL_SHARE);
      }
    }

    if (CUSTOM_EMOJI_PATTERN.matcher(contentRaw).find() || EmojiManager.containsEmoji(contentRaw)) {
      pointEvents.add(EMOJI);
    }

    if (isSurveyChannel(event.getChannel())) {
      pointEvents.add(SURVEY_MESSAGE);
    }

    int count = messageCounts.merge(user.getId(), 1, Integer::sum);
    if (count == 1) {
      pointEvents.add(FIRST_MESSAGE);
    }

    if (IntStream.rangeClosed(1, 10).map(m -> m * (m + 1) / 2).anyMatch(n -> n == count)) {
      pointEvents.add(MESSAGE);
    }

    pointApi.addPoint(user, pointEvents);
  }

  @Override
  public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
    var user = event.retrieveUser().complete();
    if (shouldIgnore(event.getGuild(), user)) {
      return;
    }

    var pointEvents = EnumSet.noneOf(PointEvent.class);

    pointEvents.add(REACTION);

    if (isSurveyChannel(event.getChannel())) {
      pointEvents.add(SURVEY_CHANNEL_REACTION);
    }

    if (isSurveyMessage(event.retrieveMessage().complete())) {
      pointEvents.add(SURVEY_MESSAGE_REACTION);
    }

    pointApi.addPoint(user, pointEvents);
  }

  @Override
  public void onChannelCreate(@NotNull ChannelCreateEvent event) {

    // スレッド作成の検知にのみ使用
    var channelType = event.getChannelType();
    if (!channelType.isThread()) {
      return;
    }

    var user = event.getGuild()
        .retrieveMemberById(event.getChannel().asThreadChannel().getOwnerId())
        .complete()
        .getUser();
    if (shouldIgnore(event.getGuild(), user)) {
      return;
    }

    var pointEvents = EnumSet.noneOf(PointEvent.class);

    if (event.getChannel().asThreadChannel().getParentChannel().getType() == ChannelType.TEXT) {
      switch (channelType) {
        case GUILD_PUBLIC_THREAD -> pointEvents.add(PUBLIC_THREAD_CREATE);
        case GUILD_PRIVATE_THREAD -> pointEvents.add(PRIVATE_THREAD_CREATE);
      }
    }

    pointApi.addPoint(user, pointEvents);
  }

  @Scheduled(cron = "${hammuni.bot.reset.cron}", zone = "${hammuni.bot.reset.zone}")
  void reset() {
    messageCounts.clear();
  }

  // ロール(everyoneを含む)へのメンションが含まれているかどうかで判定
  private boolean isSurveyMessage(@NotNull Message message) {
    return !message.getMentions().getRoles().isEmpty();
  }

  private boolean isSurveyChannel(@NotNull MessageChannelUnion channel) {
    return getChannelIDs(channel).stream().anyMatch(surveyChannels::contains);
  }

  private boolean isSharingChannel(@NotNull MessageChannelUnion channel) {
    return getChannelIDs(channel).stream().anyMatch(sharingChannels::contains);
  }

  // チャンネル自身のIDとスレッドの場合その親チャンネルのIDを返す
  private @NotNull @Unmodifiable List<@NotNull String> getChannelIDs(
      @NotNull MessageChannelUnion channel) {
    var channelID = channel.getId();
    if (channel.getType().isThread()) {
      return List.of(channelID, channel.asThreadChannel().getParentChannel().getId());
    } else {
      return List.of(channelID);
    }
  }
}
