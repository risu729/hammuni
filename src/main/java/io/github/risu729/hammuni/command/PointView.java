/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.command;

import io.github.risu729.hammuni.PointApi;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public final class PointView extends ExecutableCommandData {

  @NotNull PointApi pointApi;

  public PointView(@NotNull PointApi pointApi) {
    super(Commands.slash("point", "ポイントを確認する"));
    this.pointApi = pointApi;
  }

  @Override
  public void execute(@NotNull GenericCommandInteractionEvent event) {
    var member = checkNotNull(event.getMember());
    var apiUser = pointApi.retrieveUser(event.getUser());
    event.replyEmbeds(new EmbedBuilder().setTitle(member.getEffectiveName())
        .setThumbnail(member.getEffectiveAvatarUrl())
        .addField("食べたどんぐり🐿", String.valueOf(pointApi.retrieveConsumedPoint(apiUser.id())), false)
        .addField("持ってるどんぐり🐿", String.valueOf(apiUser.point()), false)
        .build()).queue();
  }
}
