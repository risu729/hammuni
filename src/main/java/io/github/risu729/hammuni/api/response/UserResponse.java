/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record UserResponse(@NotNull UUID id,
                           int point,
                           @Nullable List<@NotNull String> discord,
                           @Nullable List<@NotNull String> minecraft,
                           @Nullable List<@NotNull String> youtube,
                           @Nullable List<@NotNull String> twitter) {

  @Override
  public @NotNull List<@NotNull String> discord() {
    return discord == null ? Collections.emptyList() : discord;
  }

  @Override
  public @NotNull List<@NotNull String> minecraft() {
    return minecraft == null ? Collections.emptyList() : minecraft;
  }

  @Override
  public @NotNull List<@NotNull String> youtube() {
    return youtube == null ? Collections.emptyList() : youtube;
  }

  @Override
  public @NotNull List<@NotNull String> twitter() {
    return twitter == null ? Collections.emptyList() : twitter;
  }
}
