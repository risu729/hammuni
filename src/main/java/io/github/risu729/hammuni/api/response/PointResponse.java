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

import java.time.OffsetDateTime;
import java.util.UUID;

public record PointResponse(@NotNull UUID id,
                            @NotNull OffsetDateTime date,
                            int diff,
                            @Nullable UUID user,
                            @NotNull UUID app,
                            @NotNull String detail,
                            @Nullable Boolean isValid) {}
