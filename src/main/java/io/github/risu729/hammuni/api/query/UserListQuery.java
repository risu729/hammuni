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

import java.time.OffsetDateTime;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
@Accessors(fluent = true)
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public final class UserListQuery extends UserQuery {

  @Nullable Boolean random;
  @Nullable String discord;
  @Nullable String minecraft;
  @Nullable String youtube;
  @Nullable String twitter;


  @SuppressWarnings({"ConstructorWithTooManyParameters", "MethodWithMoreThanThreeNegations"})
  public UserListQuery(@Nullable Integer offset, @Nullable Integer limit, @Nullable Boolean random,
      @Nullable Boolean invalid, @Nullable OffsetDateTime since, @Nullable OffsetDateTime until,
      @Nullable Boolean positive, @Nullable Boolean negative, @Nullable String discord,
      @Nullable String minecraft, @Nullable String youtube, @Nullable String twitter) {
    super(offset, limit, invalid, since, until, positive, negative);
    checkArgument(discord == null || !discord.isBlank(), "Discord must not be blank");
    checkArgument(minecraft == null || !minecraft.isBlank(), "Minecraft must not be blank");
    checkArgument(youtube == null || !youtube.isBlank(), "Youtube must not be blank");
    checkArgument(twitter == null || !twitter.isBlank(), "Twitter must not be blank");
    this.random = random;
    this.discord = discord;
    this.minecraft = minecraft;
    this.youtube = youtube;
    this.twitter = twitter;
  }

  @Contract(pure = true)
  @Override
  protected @NotNull ToQueryMapHelper toQueryMapHelper() {
    return super.toQueryMapHelper()
        .add("random", random)
        .add("discord", discord)
        .add("minecraft", minecraft)
        .add("youtube", youtube)
        .add("twitter", twitter);
  }
}
