package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class DateTimeTypeConverter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
  @Override
  public JsonElement serialize(@NonNull final DateTime src, @NonNull final Type srcType,
    @NonNull final JsonSerializationContext context) {
    return new JsonPrimitive(src.getMillis() / 1000);
  }

  @Override
  public DateTime deserialize(@NonNull final JsonElement json, @NonNull final Type type,
    @NonNull final JsonDeserializationContext context) {
    return new DateTime(json.getAsInt() * 1000L);
  }
}
