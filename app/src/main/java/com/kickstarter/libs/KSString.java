package com.kickstarter.libs;

import android.content.res.Resources;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class KSString {
  private final String packageName;
  private final Resources resources;

  public KSString(final @NonNull String packageName, final @NonNull Resources resources) {
    this.packageName = packageName;
    this.resources = resources;
  }

  /**
   * Replace each key found in the string with its corresponding value.
   */
  public @NonNull String format(final @NonNull String string, final @NonNull String key1, final @Nullable String value1) {
    final Map<String, String> substitutions = new HashMap<String, String>() {
      {
        put(key1, value1);
      }
    };
    return replace(string, substitutions);
  }

  /**
   * Replace each key found in the string with its corresponding value.
   */
  public @NonNull String format(final @NonNull String string,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2) {
    final Map<String, String> substitutions = new HashMap<String, String>() {
      {
        put(key1, value1);
        put(key2, value2);
      }
    };
    return replace(string, substitutions);
  }

  /**
   * Replace each key found in the string with its corresponding value.
   */
  public @NonNull String format(final @NonNull String string,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2,
    final @NonNull String key3, final @Nullable String value3) {
    final Map<String, String> substitutions = new HashMap<String, String>() {
      {
        put(key1, value1);
        put(key2, value2);
        put(key3, value3);
      }
    };
    return replace(string, substitutions);
  }

  /**
   * Replace each key found in the string with its corresponding value.
   */
  public @NonNull String format(final @NonNull String string,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2,
    final @NonNull String key3, final @Nullable String value3,
    final @NonNull String key4, final @Nullable String value4) {
    final Map<String, String> substitutions = new HashMap<String, String>() {
      {
        put(key1, value1);
        put(key2, value2);
        put(key3, value3);
        put(key4, value4);
      }
    };
    return replace(string, substitutions);
  }

  /**
   * Given a base key path and count, find the appropriate string resource and replace each key
   * found in the string resource with its corresponding value. For example, given a base key of `foo`,
   * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
   *
   * This particular version is for strings that have no replaceable sections
   */
  public @NonNull String format(final @NonNull String baseKeyPath, final int count) {
    return stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count));
  }

  /**
   * Given a base key path and count, find the appropriate string resource and replace each key
   * found in the string resource with its corresponding value. For example, given a base key of `foo`,
   * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
   */
  public @NonNull String format(final @NonNull String baseKeyPath, final int count,
    final @NonNull String key1, final @Nullable String value1) {
    final String string = stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count));
    return format(string, key1, value1);
  }

  /**
   * Given a base key path and count, find the appropriate string resource and replace each key
   * found in the string resource with its corresponding value. For example, given a base key of `foo`,
   * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
   */
  public @NonNull String format(final @NonNull String baseKeyPath, final int count,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2) {
    final String string = stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count));
    return format(string, key1, value1, key2, value2);
  }

  /**
   * Given a base key path and count, find the appropriate string resource and replace each key
   * found in the string resource with its corresponding value. For example, given a base key of `foo`,
   * a count of 0 would give the string resource `foo_zero`, a count of 1 `foo_one`, and so on.
   */
  public @NonNull String format(final @NonNull String baseKeyPath, final int count,
    final @NonNull String key1, final @Nullable String value1,
    final @NonNull String key2, final @Nullable String value2,
    final @NonNull String key3, final @Nullable String value3) {
    final String string = stringFromKeyPath(baseKeyPath, keyPathComponentForCount(count));
    return format(string, key1, value1, key2, value2, key3, value3);
  }

  /**
   * Takes a variable length of {@link String} arguments, joins them together to form a single path, then
   * looks up a string resource given that path. If the resource cannot be found, returns an empty string.
   */
  private @NonNull String stringFromKeyPath(final @NonNull String... keyPathComponents) {
    final String keyPath = TextUtils.join("_", keyPathComponents);
    try {
      final int resourceId = this.resources.getIdentifier(keyPath, "string", this.packageName);
      return this.resources.getString(resourceId);
    } catch (final @NonNull Resources.NotFoundException e) {
      return "";
    }
  }

  private @Nullable String keyPathComponentForCount(final int count) {
    if (count == 0) {
      return "zero";
    } else if (count == 1) {
      return "one";
    } else if (count == 2) {
      return "two";
    } else if (count > 2 && count <= 5) {
      return "few";
    } else if (count > 5) {
      return "many";
    }

    return null;
  }

  /**
   * For a given string, replaces occurrences of each key with its corresponding value. In the string, keys are wrapped
   * with `%{}`, e.g. `%{backers_count} backers`. In this instance, the substitutions hash might contain one entry with the key
   * `backers_count` and value `2`.
   */
  private @NonNull String replace(final @NonNull String string, final @NonNull Map<String, String> substitutions) {
    final StringBuilder builder = new StringBuilder();
    for (final String key : substitutions.keySet()) {
      if (builder.length() > 0) {
        builder.append("|");
      }
      builder
        .append("(%\\{")
        .append(key)
        .append("\\})");
    }

    final Pattern pattern = Pattern.compile(builder.toString());
    final Matcher matcher = pattern.matcher(string);
    final StringBuffer buffer = new StringBuffer();

    while (matcher.find()) {
      final String key = NON_WORD_REGEXP.matcher(matcher.group()).replaceAll("");
      final String value = substitutions.get(key);
      final String replacement = Matcher.quoteReplacement(value != null ? value : "");
      matcher.appendReplacement(buffer, replacement);
    }
    matcher.appendTail(buffer);

    return buffer.toString();
  }

  private static final Pattern NON_WORD_REGEXP = Pattern.compile("[^\\w]");
}
