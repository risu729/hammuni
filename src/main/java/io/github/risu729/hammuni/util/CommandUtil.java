/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.util;

import com.google.common.collect.Streams;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@UtilityClass
public class CommandUtil {

  @Contract(pure = true)
  public @NotNull List<@NotNull OptionData> createVarArgsOption(@NotNull OptionData option,
      int requiredCount, int optionalCount) {
    var data = option.toData();
    return Streams.mapWithIndex(Stream.generate(() -> OptionData.fromData(data)),
        (optionData, index) -> optionData.setName(option.getName() + (index + 1))
            .setRequired(index + 1 <= requiredCount)).limit(requiredCount + optionalCount).toList();
  }
}
