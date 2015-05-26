package com.kickstarter.libs;

import com.google.common.base.Enums;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.kickstarter.models.Activity;

import java.lang.reflect.Type;

public class ActivityCategoryTypeConverter implements JsonSerializer<Activity.Category>, JsonDeserializer<Activity.Category> {
  @Override
  public JsonElement serialize(final Activity.Category src,
    final Type srcType,
    final JsonSerializationContext context) {
    return new JsonPrimitive(src.toString().toLowerCase());
  }

  @Override
  public Activity.Category deserialize(
    final JsonElement json,
    final Type type,
    final JsonDeserializationContext context) {
    return Enums.getIfPresent(Activity.Category.class, json.getAsString().toUpperCase()).or(Activity.Category.UNKNOWN);

  }
}
