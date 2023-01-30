/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.request;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

public record PointRequest(int diff,
                           @NotNull UUID user,
                           @NotNull UUID app,
                           @NotNull String detail) {

  public PointRequest {
    checkArgument(diff != 0, "Diff cannot be 0");
    checkArgument(!detail.isBlank(), "Detail cannot be blank");
  }
}
