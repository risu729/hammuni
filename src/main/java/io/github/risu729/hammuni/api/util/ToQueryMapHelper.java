/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class ToQueryMapHelper {

  Map<String, String> map = new HashMap<>();

  public @NotNull ToQueryMapHelper add(@NotNull String name, @Nullable Object value) {
    if (value != null) {
      map.put(name, value.toString());
    }
    return this;
  }

  @CheckReturnValue
  public @NotNull Map<String, String> toQueryMap() {
    return Collections.unmodifiableMap(map);
  }
}
