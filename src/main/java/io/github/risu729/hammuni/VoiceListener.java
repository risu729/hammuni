/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.risu729.hammuni.EventFilter.shouldIgnore;
import static io.github.risu729.hammuni.PointEvent.FIRST_VC;
import static io.github.risu729.hammuni.PointEvent.VC;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class VoiceListener extends ListenerAdapter {

  @NotNull Map<String, Integer> voiceCounts = new HashMap<>();
  @NotNull Map<String, Future<?>> voiceFutures = new HashMap<>();
  @NotNull PointApi pointApi;
  @NotNull ScheduledExecutorService scheduler;
  @NotNull String guildId;
  @NonFinal
  @Setter(AccessLevel.PACKAGE)
  @Nullable JDA jda;

  VoiceListener(@NotNull PointApi pointApi, @NotNull ScheduledExecutorService scheduler,
      @Value("${hammuni.bot.guild}") @NotNull String guildId) {
    this.pointApi = pointApi;
    this.scheduler = scheduler;
    this.guildId = guildId;
  }

  @Override
  public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
    var user = event.getMember().getUser();
    if (shouldIgnore(event.getGuild(), user)) {
      return;
    }

    var userID = user.getId();

    // on voice join
    if (event.getChannelLeft() == null && event.getChannelJoined() != null) {
      onVoiceJoin(userID);
    }

    // on voice leave
    if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
      Optional.ofNullable(voiceFutures.remove(userID)).ifPresent(future -> future.cancel(false));
    }
  }

  @SuppressWarnings("MagicNumber")
  void onVoiceJoin(@NotNull String userId) {

    if (!voiceCounts.containsKey(userId)) {
      incrementVoiceCount(userId);
      pointApi.addPoint(userId, FIRST_VC);
    }

    // 最大5回/日 VCに入っている間、30分ごとにポイントを付与
    voiceFutures.put(userId, scheduler.scheduleAtFixedRate(() -> {
      if (incrementVoiceCount(userId) <= 6) {
        pointApi.addPoint(userId, VC);
      }
    }, 30, 30, TimeUnit.MINUTES));
  }

  @Scheduled(cron = "${hammuni.bot.reset.cron}", zone = "${hammuni.bot.reset.zone}")
  void clear() {
    voiceCounts.clear();
    voiceFutures.values().forEach(future -> future.cancel(false));
    voiceFutures.clear();
    // リセット時にVCにいる人はその時点で入ったとみなす
    checkNotNull(checkNotNull(jda).getGuildById(guildId)).getVoiceStates()
        .stream()
        .filter(GuildVoiceState::inAudioChannel)
        .map(GuildVoiceState::getMember)
        .map(Member::getUser)
        .map(User::getId)
        .forEach(this::onVoiceJoin);
  }

  private int incrementVoiceCount(@NotNull String userId) {
    return voiceCounts.merge(userId, 1, Integer::sum);
  }
}
