/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.api.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

  private final @NotNull DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
      "uuuu-MM-dd_HH:mm:ss");

  @Override
  public void write(@NotNull JsonWriter jsonWriter, @NotNull LocalDateTime offsetDateTime)
      throws IOException {
    jsonWriter.value(offsetDateTime.format(formatter));
  }

  @Override
  public @NotNull LocalDateTime read(@NotNull JsonReader jsonReader) throws IOException {
    return LocalDateTime.parse(jsonReader.nextString(), formatter);
  }
}
