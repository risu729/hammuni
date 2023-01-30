/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api;

import com.google.gson.GsonBuilder;
import io.github.risu729.hammuni.api.util.OffsetDateTimeAdapter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.OffsetDateTime;
import java.util.Base64;

@Configuration
public class ClientConfiguration {

  @SuppressWarnings("LoggerInitializedWithForeignClass")
  @Bean
  public PointApiClient pointsApiClient(@NotNull OffsetDateTimeAdapter offsetDateTimeAdapter,
      @Value("${hammuni.api.url}") String url, @Value("${hammuni.api.username}") String username,
      @Value("${hammuni.api.password}") String password) {
    return new Retrofit.Builder().baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().registerTypeAdapter(
            OffsetDateTime.class,
            offsetDateTimeAdapter).create()))
        .client(new OkHttpClient.Builder().addInterceptor(chain -> chain.proceed(chain.request()
                .newBuilder()
                .header("Authorization",
                    "Basic " + Base64.getEncoder()
                        .encodeToString((username + ":" + password).getBytes()))
                .build()))
            .addInterceptor(new HttpLoggingInterceptor(LoggerFactory.getLogger(PointApiClient.class)::info).setLevel(
                HttpLoggingInterceptor.Level.BODY))
            .build())
        .build()
        .create(PointApiClient.class);
  }
}
