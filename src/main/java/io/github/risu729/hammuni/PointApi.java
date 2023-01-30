/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import com.google.common.collect.MoreCollectors;
import io.github.risu729.hammuni.api.PointApiClient;
import io.github.risu729.hammuni.api.query.UserListQuery;
import io.github.risu729.hammuni.api.query.UserQuery;
import io.github.risu729.hammuni.api.request.PointRequest;
import io.github.risu729.hammuni.api.request.UserRequest;
import io.github.risu729.hammuni.api.response.UserResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;


@SuppressWarnings("DataFlowIssue")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class PointApi {

  @NotNull UUID appId;
  @NotNull String detailPrefix;
  @NotNull PointApiClient pointApiClient;

  private PointApi(@Value("${hammuni.api.app}") @NotNull UUID appId,
      @Value("${hammuni.api.detail-prefix}") @NotNull String detailPrefix,
      @NotNull PointApiClient pointApiClient) {
    this.appId = appId;
    this.detailPrefix = detailPrefix;
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
      if (apiUser.point() < -pointEvent.gainPoint() + pointEvent.consumePoint()) {
        return;
      }
      IntStream.of(pointEvent.gainPoint(), -pointEvent.consumePoint())
          .filter(point -> point != 0)
          .mapToObj(point -> new PointRequest(point,
              apiUser.id(),
              appId,
              detailPrefix + pointEvent.name().toLowerCase(Locale.ENGLISH)))
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
}
