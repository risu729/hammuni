/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.query;

import io.github.risu729.hammuni.api.util.ToQueryMapHelper;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

@Accessors(fluent = true)
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public sealed class ListQuery extends Query permits UserQuery {

  @Nullable Integer offset;
  @Nullable Integer limit;

  public ListQuery(@Nullable Integer offset, @Nullable Integer limit) {
    checkArgument(offset == null || offset >= 0, "Offset must be greater than or equal to 0");
    checkArgument(limit == null || limit > 0, "Limit must be greater than 0");
    this.offset = offset;
    this.limit = limit;
  }

  protected @NotNull ToQueryMapHelper toQueryMapHelper() {
    return super.toQueryMapHelper().add("offset", offset).add("limit", limit);
  }
}
