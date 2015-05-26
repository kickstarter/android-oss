package com.kickstarter.libs;

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
  public JsonElement serialize(final DateTime src, final Type srcType, final JsonSerializationContext context) {
    return new JsonPrimitive(src.getMillis() / 1000);
  }

  @Override
  public DateTime deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) {
    return new DateTime(json.getAsInt() * 1000L);
  }
}
