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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {

  private final @NotNull DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  @Override
  public void write(@NotNull JsonWriter jsonWriter, @NotNull OffsetDateTime offsetDateTime)
      throws IOException {
    jsonWriter.value(offsetDateTime.format(formatter));
  }

  @Override
  public @NotNull OffsetDateTime read(@NotNull JsonReader jsonReader) throws IOException {
    return OffsetDateTime.parse(jsonReader.nextString(), formatter);
  }
}
