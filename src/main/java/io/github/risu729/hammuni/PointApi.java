/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import com.google.common.collect.MoreCollectors;
import com.google.common.primitives.Ints;
import io.github.risu729.hammuni.api.PointApiClient;
import io.github.risu729.hammuni.api.query.UserListQuery;
import io.github.risu729.hammuni.api.query.UserQuery;
import io.github.risu729.hammuni.api.request.PointRequest;
import io.github.risu729.hammuni.api.request.UserRequest;
import io.github.risu729.hammuni.api.response.ListResponse;
import io.github.risu729.hammuni.api.response.PointResponse;
import io.github.risu729.hammuni.api.response.UserResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings("DataFlowIssue")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class PointApi {

  @NotNull UUID appId;
  @NotNull String detailPrefix;
  @NotNull LocalTime resetTime;
  @NotNull ZoneId zoneId;
  @NotNull PointApiClient pointApiClient;

  private PointApi(@Value("${hammuni.api.app}") @NotNull UUID appId,
      @Value("${hammuni.api.detail-prefix}") @NotNull String detailPrefix,
      @Value("${hammuni.bot.reset.time}") @NotNull String resetTime,
      @Value("${hammuni.bot.reset.zone}") @NotNull String zoneId,
      @NotNull PointApiClient pointApiClient) {
    this.appId = appId;
    this.detailPrefix = detailPrefix;
    this.resetTime = LocalTime.parse(resetTime);
    this.zoneId = ZoneId.of(zoneId);
    this.pointApiClient = pointApiClient;
  }

  public @NotNull UserResponse retrieveUser(@NotNull User user) {
    return retrieveUser(user.getId());
  }

  public @NotNull UserResponse retrieveUser(@NotNull String discordUserId) {
    try {
      // 複数ユーザーがDiscord IDに紐づけられている際に例外を投げるよう最大2件取得する
      return pointApiClient.getUsers(UserListQuery.builder()
              .discord(discordUserId)
              .limit(2)
              .build())
          .execute()
          .body()
          .results()
          .stream()
          .collect(MoreCollectors.toOptional())
          .orElseGet(() -> {
            try {
              pointApiClient.createUser(UserRequest.builder()
                  .discord(List.of(discordUserId))
                  .build()).execute();
              addPoint(discordUserId, PointEvent.SERVER_JOIN);
              return retrieveUser(discordUserId);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public int retrieveConsumedPoint(@NotNull UUID userId) {
    try {
      return -pointApiClient.getUser(userId, UserQuery.builder().negative(true).build())
          .execute()
          .body()
          .point();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void addPoint(@NotNull User user, @NotNull Collection<@NotNull PointEvent> pointEvents) {
    addPoint(user.getId(), pointEvents);
  }

  public void addPoint(@NotNull User user, @NotNull PointEvent pointEvent) {
    addPoint(user, List.of(pointEvent));
  }

  public void addPoint(@NotNull String userId, @NotNull PointEvent pointEvent) {
    addPoint(userId, List.of(pointEvent));
  }

  public void addPoint(@NotNull String userId,
      @NotNull Collection<@NotNull PointEvent> pointEvents) {
    var apiUser = retrieveUser(userId);
    for (var pointEvent : pointEvents) {
      if (!hasEnoughPoint(apiUser, pointEvent)) {
        if (pointEvent.rejectIfNotEnough()) {
          throw new IllegalStateException("User %s does not have enough point for the event %s".formatted(
              userId,
              pointEvent));
        } else {
          return;
        }
      }
      IntStream.of(pointEvent.gainPoint(), -pointEvent.consumePoint())
          .filter(point -> point != 0)
          .mapToObj(point -> new PointRequest(point,
              apiUser.id(),
              appId,
              detailPrefix + pointEvent))
          .map(pointApiClient::addPoint)
          .forEach(call -> {
            try {
              call.execute();
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    }
  }

  public boolean hasEnoughPoint(@NotNull User user, @NotNull PointEvent pointEvent) {
    return hasEnoughPoint(user.getId(), pointEvent);
  }

  public boolean hasEnoughPoint(@NotNull String userId, @NotNull PointEvent pointEvent) {
    var apiUser = retrieveUser(userId);
    return hasEnoughPoint(apiUser, pointEvent);
  }

  private boolean hasEnoughPoint(@NotNull UserResponse apiUser, @NotNull PointEvent pointEvent) {
    return apiUser.point() >= -pointEvent.gainPoint() + pointEvent.consumePoint();
  }

  public int retrieveCountSinceReset(@NotNull User user, @NotNull PointEvent pointEvent) {
    return retrieveCountSinceReset(user.getId(), pointEvent);
  }

  @SuppressWarnings("MagicNumber")
  public int retrieveCountSinceReset(@NotNull String userId, @NotNull PointEvent pointEvent) {
    var apiUser = retrieveUser(userId);
    // 今日のリセット時刻を過ぎていれば今日、過ぎていなければ昨日のリセット時刻を取得する
    var lastReset = ZonedDateTime.of(LocalDate.now(zoneId)
            .minusDays(LocalTime.now(zoneId).isAfter(resetTime) ? 0 : 1), resetTime, zoneId)
        .toLocalDateTime();
    try {
      return Ints.checkedCast(Stream.iterate(pointApiClient.getUserHistory(apiUser.id(),
              UserQuery.builder()
                  .since(lastReset)
                  .limit(50) // できる限り1回のAPI呼び出しでリセットしてからの履歴を取得できるように (デフォルトは10件)
                  .build()).execute().body(), response -> response.next() != null, response -> {
            try {
              return pointApiClient.getUserHistory(response.next()).execute().body();
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          })
          .map(ListResponse::results)
          .flatMap(List::stream)
          .filter(response -> response.app().equals(appId))
          .map(PointResponse::detail)
          .filter(detail -> detail.equals(detailPrefix + pointEvent))
          .count());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void linkUsers(@NotNull List<? extends @NotNull User> discordUsers) {
    var apiUsers = discordUsers.stream()
        .map(User::getId)
        .map(this::retrieveUser)
        .distinct()
        .toList();
    // 既に紐づけられている場合同一のユーザーが返されるので、その場合は何もしない
    if (apiUsers.size() == 1) {
      return;
    }
    var mainUser = apiUsers.stream()
        .max(Comparator.comparingInt(UserResponse::point))
        .orElseThrow();
    try {
      pointApiClient.updateUser(mainUser.id(),
          UserRequest.builder()
              .discord(apiUsers.stream()
                  .map(UserResponse::discord)
                  .flatMap(List::stream)
                  .distinct()
                  .toList())
              .build()).execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    var subUsers = apiUsers.stream().filter(user -> !user.id().equals(mainUser.id())).toList();
    subUsers.stream().map(UserResponse::id).map(pointApiClient::deleteUser).forEach(call -> {
      try {
        call.execute();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
    subUsers.stream().mapToInt(UserResponse::point).forEach(point -> {
      if (point != 0) {
        try {
          pointApiClient.addPoint(new PointRequest(point,
              mainUser.id(),
              appId,
              detailPrefix + "link_users")).execute();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    });
  }
}
