package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class MapUtils {
  private MapUtils() {}

  @NonNull public static <T> Map<String, T> prefixKeys(@NonNull final Map<String, T> source, @NonNull final String prefix) {
    // Early out on a case that can come up often.
    if (prefix.equals("")) {
      return source;
    }

    final Map<String, T> result = new HashMap<>();

    for (final String key : source.keySet()) {
      result.put(prefix + key, source.get(key));
    }

    return result;
  }

  @NonNull public static <S, T> Map<S, T> compact(@NonNull final Map<S, T> source) {
    final Map<S, T> output = new HashMap<>(source);
    output.values().remove(null);
    return output;
  }

  @NonNull public static <S, T> Map<S, T> empty() {
    return new HashMap<>();
  }
}
