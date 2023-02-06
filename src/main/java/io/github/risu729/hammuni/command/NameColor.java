/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.command;

import com.google.common.collect.MoreCollectors;
import io.github.risu729.hammuni.PointApi;
import io.github.risu729.hammuni.PointEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class NameColor extends ExecutableCommandData {

  @NotNull PointApi pointApi;

  public NameColor(@NotNull PointApi pointApi) {
    super(Commands.slash("buycolor", "名前の色を交換する")
        .addSubcommands(Arrays.stream(ColorType.values()).map(ColorType::subcommandData).toList()));
    this.pointApi = pointApi;
  }

  @Override
  public void execute(@NotNull InteractionHook hook,
      @NotNull GenericCommandInteractionEvent event) {

    var user = event.getUser();
    var colorType = ColorType.fromSubcommandName(checkNotNull(event.getSubcommandName()));
    var userResponse = pointApi.retrieveUser(user);

    var pointEvent = colorType.pointEvent();
    if (!pointApi.hasEnoughPoint(user, colorType.pointEvent())) {
      hook.sendMessageEmbeds(new EmbedBuilder().setTitle("ポイント不足😿")
          .setDescription("あと%dどんぐり必要です…".formatted(
              pointEvent.consumePoint() - userResponse.point()))
          .build()).setEphemeral(true).queue();
      return;
    }

    pointApi.addPoint(user, pointEvent);

    var guild = checkNotNull(event.getGuild());
    guild.addRoleToMember(user, colorType.role(guild)).queue();

    event.replyEmbeds(new EmbedBuilder().setTitle("変更成功")
        .setDescription("名前の色を変更しました🎉")
        .build()).queue();
  }

  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  @Accessors(fluent = true)
  @Getter(AccessLevel.PRIVATE)
  @AllArgsConstructor
  private enum ColorType {
    // 宣言順にロールが並べられる
    GOLD("gold", "金", "金のロール", Color.decode("#d1c808"), PointEvent.GOLD_ROLE),
    SILVER("silver", "銀", "銀のロール", Color.decode("#b1b1b1"), PointEvent.SILVER_ROLE),
    ;

    String subcommandName;
    String subcommandDescription;
    String roleName;
    Color color;
    PointEvent pointEvent;

    @Contract(pure = true)
    private static @NotNull ColorType fromSubcommandName(@NotNull String subcommandName) {
      return Arrays.stream(values())
          .filter(colorType -> colorType.subcommandName().equals(subcommandName))
          .collect(MoreCollectors.onlyElement());
    }

    @Contract(value = " -> new", pure = true)
    private @NotNull SubcommandData subcommandData() {
      return new SubcommandData(subcommandName, subcommandDescription);
    }

    @CheckReturnValue
    private @NotNull Role role(@NotNull Guild guild) {
      var role = guild.getRolesByName(roleName, false)
          .stream()
          .collect(MoreCollectors.toOptional())
          .orElseGet(() -> guild.createRole().setName(roleName).setColor(color).complete());
      var aboveRole = ordinal() == 0
          ? checkNotNull(guild.getRoleByBot(guild.getSelfMember().getUser()))
          : values()[ordinal() - 1].role(guild);
      // 自身のロールが一番上として、その下に配置することで他のロールより上となり色が優先される
      guild.modifyRolePositions().selectPosition(role).moveBelow(aboveRole).queue();
      return role;
    }
  }
}
