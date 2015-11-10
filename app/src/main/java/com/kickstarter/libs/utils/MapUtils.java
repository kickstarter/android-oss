package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class MapUtils {
  private MapUtils() {}

  @NonNull public static <T> Map<String, T> prefixKeys(@NonNull final Map<String, T> source, @NonNull final String prefix) {

    final Map<String, T> result = new HashMap<>();

    for (final String key : source.keySet()) {
      result.put(prefix + key, source.get(key));
    }

    return result;
  }
}
