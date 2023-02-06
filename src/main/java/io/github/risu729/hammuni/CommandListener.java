/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import com.google.common.collect.MoreCollectors;
import io.github.risu729.hammuni.command.ExecutableCommandData;
import io.github.risu729.hammuni.command.LinkAccounts;
import io.github.risu729.hammuni.command.NameColor;
import io.github.risu729.hammuni.command.PointView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
@ToString
public class CommandListener extends ListenerAdapter {

  @NotNull List<? extends @NotNull ExecutableCommandData> commands;

  public CommandListener(@NotNull PointView pointView, @NotNull LinkAccounts linkAccounts,
      @NotNull NameColor nameColor) {
    commands = List.of(pointView, linkAccounts, nameColor);
  }

  @Override
  public final void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
    // タイムアウト防止のため、deferReplyを先に実行
    event.deferReply().queue();
    commands.stream()
        .filter(command -> command.getName().equals(event.getName()))
        .collect(MoreCollectors.onlyElement())
        .execute(event.getHook(), event);
  }
}
