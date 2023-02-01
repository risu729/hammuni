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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
@Accessors(fluent = true)
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public sealed class UserQuery extends ListQuery permits UserListQuery {

  @Nullable Boolean invalid;
  @Nullable LocalDateTime since;
  @Nullable LocalDateTime until;
  @Nullable Boolean positive;
  @Nullable Boolean negative;

  @SuppressWarnings("ConstructorWithTooManyParameters")
  public UserQuery(@Nullable Integer offset, @Nullable Integer limit,
      @Nullable Boolean invalid, @Nullable LocalDateTime since, @Nullable LocalDateTime until,
      @Nullable Boolean positive, @Nullable Boolean negative) {
    super(offset, limit);
    checkArgument(!(Boolean.TRUE.equals(positive) && Boolean.TRUE.equals(negative)),
        "Positive and negative cannot be both true");
    this.invalid = invalid;
    this.since = since;
    this.until = until;
    this.positive = positive;
    this.negative = negative;
  }

  @Contract(pure = true)
  @Override
  protected @NotNull ToQueryMapHelper toQueryMapHelper() {
    return super.toQueryMapHelper()
        .add("invalid", invalid)
        .add("since", since)
        .add("until", until)
        .add("positive", positive)
        .add("negative", negative);
  }
}
