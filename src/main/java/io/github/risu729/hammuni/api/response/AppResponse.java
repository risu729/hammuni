/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.response;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record AppResponse(@NotNull UUID id, @NotNull String name) {}
