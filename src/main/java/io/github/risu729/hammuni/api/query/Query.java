/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.query;

import io.github.risu729.hammuni.api.util.ToQueryMapHelper;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

@SuppressWarnings("ALL")
@SuperBuilder
@NoArgsConstructor // to avoid exception when using @SuperBuilder
class Query {

  @Contract(pure = true)
  public final @NotNull @Unmodifiable Map<String, String> toQueryMap() {
    return toQueryMapHelper().toQueryMap();
  }

  @Contract(pure = true)
  protected @NotNull ToQueryMapHelper toQueryMapHelper() {
    return new ToQueryMapHelper();
  }
}
