/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.request;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkArgument;

public record AppRequest(@NotNull String name) {

  public AppRequest {
    checkArgument(!name.isBlank(), "Name cannot be blank");
  }
}
