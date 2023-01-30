/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.command;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class ExecutableCommandData implements CommandData {

  @Delegate
  @NotNull CommandData command;

  public abstract void execute(@NotNull GenericCommandInteractionEvent event);
}
