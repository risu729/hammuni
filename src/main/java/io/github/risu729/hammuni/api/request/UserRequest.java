/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.request;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Builder
public record UserRequest(@Nullable List<@NotNull String> discord,
                          @Nullable List<@NotNull String> minecraft,
                          @Nullable List<@NotNull String> youtube,
                          @Nullable List<@NotNull String> twitter) {

  public UserRequest {
    checkList(discord);
    checkList(minecraft);
    checkList(youtube);
    checkList(twitter);
  }

  @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
  private void checkList(@Nullable List<@NotNull String> list) {
    if (list == null) {
      return;
    }
    checkArgument(!list.isEmpty());
    checkArgument(list.stream().noneMatch(String::isBlank));
  }
}
