/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api;

import io.github.risu729.hammuni.api.query.ListQuery;
import io.github.risu729.hammuni.api.query.UserListQuery;
import io.github.risu729.hammuni.api.query.UserQuery;
import io.github.risu729.hammuni.api.request.AppRequest;
import io.github.risu729.hammuni.api.request.PointRequest;
import io.github.risu729.hammuni.api.request.UserRequest;
import io.github.risu729.hammuni.api.response.AppResponse;
import io.github.risu729.hammuni.api.response.ListResponse;
import io.github.risu729.hammuni.api.response.PointResponse;
import io.github.risu729.hammuni.api.response.UserResponse;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public interface PointApiClient {

  @GET("api/app/")
  Call<ListResponse<AppResponse>> getApps(@QueryMap @NotNull Map<String, String> query);

  default Call<ListResponse<AppResponse>> getApps(@NotNull ListQuery query) {
    return getApps(query.toQueryMap());
  }

  default Call<ListResponse<AppResponse>> getApps() {
    return getApps(Collections.emptyMap());
  }

  @POST("api/app/")
  Call<AppResponse> createApp(@Body @NotNull AppRequest appRequest);

  @POST("api/point/")
  Call<PointResponse> addPoint(@Body @NotNull PointRequest pointRequest);

  @GET("api/users/")
  Call<ListResponse<UserResponse>> getUsers(@QueryMap @NotNull Map<String, String> query);

  default Call<ListResponse<UserResponse>> getUsers(@NotNull UserListQuery query) {
    return getUsers(query.toQueryMap());
  }

  default Call<ListResponse<UserResponse>> getUsers() {
    return getUsers(Collections.emptyMap());
  }

  @POST("api/users/")
  Call<UserResponse> createUser(@Body @NotNull UserRequest userRequest);

  @GET("api/users/{UUID}/")
  Call<UserResponse> getUser(@Path("UUID") @NotNull UUID uuid);

  @GET("api/users/{UUID}/")
  Call<UserResponse> getUser(@Path("UUID") @NotNull UUID uuid,
      @QueryMap @NotNull Map<String, String> query);

  default Call<UserResponse> getUser(@NotNull UUID uuid, @NotNull UserQuery query) {
    return getUser(uuid, query.toQueryMap());
  }

  @PATCH("api/users/{UUID}/")
  Call<UserResponse> updateUser(@Path("UUID") @NotNull UUID uuid,
      @Body @NotNull UserRequest userRequest);

  @DELETE("api/users/{UUID}/")
  Call<UserResponse> deleteUser(@Path("UUID") @NotNull UUID uuid);

  @GET("api/users/{UUID}/history/")
  Call<ListResponse<PointResponse>> getUserHistory(@Path("UUID") @NotNull UUID uuid,
      @QueryMap @NotNull Map<String, String> query);

  default Call<ListResponse<PointResponse>> getUserHistory(@NotNull UUID uuid,
      @NotNull UserQuery query) {
    return getUserHistory(uuid, query.toQueryMap());
  }

  default Call<ListResponse<PointResponse>> getUserHistory(@NotNull UUID uuid) {
    return getUserHistory(uuid, Collections.emptyMap());
  }
}
