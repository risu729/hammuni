/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings({"UtilityClassWithoutPrivateConstructor", "NonFinalUtilityClass"})
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class EventFilter {

  private static @Nullable String guildId;

  @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
  EventFilter(@Value("${hammuni.bot.guild}") @NotNull String guildId) {
    EventFilter.guildId = guildId;
  }


  @SuppressWarnings("StaticVariableUsedBeforeInitialization")
  static boolean shouldIgnore(@Nullable Guild guild, @NotNull User user) {
    return
        Optional.ofNullable(guild).map(Guild::getId).filter(checkNotNull(guildId)::equals).isEmpty()
            || user.isBot() || user.isSystem();
  }
}
