package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KSString {
  private KSString() {}

  public static @NonNull String format(final @NonNull String string, final @NonNull String key1, final @Nullable String value1) {
    Map<String, String> substitutions = new HashMap<String, String>() {{
      put(key1, value1);
    }};
    return replace(string, substitutions);
  }

  public static @NonNull String format(final @NonNull String string,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2) {
    final Map<String, String> substitutions = new HashMap<String, String>() {{
      put(key1, value1);
      put(key2, value2);
    }};
    return replace(string, substitutions);
  }

  public static @NonNull String format(final @NonNull String string,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2,
    final @NonNull String key3, final @Nullable String value3) {
    final Map<String, String> substitutions = new HashMap<String, String>() {{
      put(key1, value1);
      put(key2, value2);
      put(key3, value3);
    }};
    return replace(string, substitutions);
  }

  public static @NonNull String format(final @NonNull String string,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2,
    final @NonNull String key3, final @Nullable String value3,
    final @NonNull String key4, final @Nullable String value4) {
    final Map<String, String> substitutions = new HashMap<String, String>() {{
      put(key1, value1);
      put(key2, value2);
      put(key3, value3);
      put(key4, value4);
    }};
    return replace(string, substitutions);
  }

  private static @NonNull String replace(final @NonNull String string, final Map<String, String> substitutions) {
    final StringBuilder builder = new StringBuilder();
    for (final Map.Entry<String, String> entry : substitutions.entrySet()) {
      if (builder.length() > 0) {
        builder.append("|");
      }
      // TODO: replace with string format
      builder
        .append("(%\\{")
        .append(entry.getKey())
        .append("\\})");
    }

    final Pattern pattern = Pattern.compile(builder.toString());
    final Matcher matcher = pattern.matcher(string);
    final StringBuffer buffer = new StringBuffer();

    while (matcher.find()) {
      final String key = matcher.group().replaceAll("[^\\w]", ""); // TODO: Could compile this into a pattern
      final String value = substitutions.get(key);
      final String replacement = value != null ? value : "";
      matcher.appendReplacement(buffer, replacement);
    }
    matcher.appendTail(buffer);

    return buffer.toString();
  }
}
