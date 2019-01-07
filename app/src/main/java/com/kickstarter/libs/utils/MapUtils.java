package com.kickstarter.libs.utils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public final class MapUtils {
  private MapUtils() {}

  /**
   * Returns a new map with all keys prefixed with another string.
   */
  @NonNull public static <T> Map<String, T> prefixKeys(final @NonNull Map<String, T> source, final @NonNull String prefix) {
    // Early out on a case that can come up often.
    if ("".equals(prefix)) {
      return source;
    }

    final Map<String, T> result = new HashMap<>();

    for (final String key : source.keySet()) {
      result.put(prefix + key, source.get(key));
    }

    return result;
  }

  /**
   * Returns a new map with all `null` values removed.
   */
  @NonNull public static <S, T> Map<S, T> compact(final @NonNull Map<S, T> source) {
    final Map<S, T> output = new HashMap<>(source);
    for (final S key : source.keySet()) {
      if (source.get(key) == null) {
        output.remove(key);
      }
    }
    return output;
  }

  /**
   * Returns an empty map.
   */
  @NonNull public static <S, T> Map<S, T> empty() {
    return new HashMap<>();
  }
}
