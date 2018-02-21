package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;

public final class StringUtils {
  private StringUtils() {}

  public static boolean isEmail(final @NonNull CharSequence str) {
    return Patterns.EMAIL_ADDRESS.matcher(str).matches();
  }

  public static boolean isEmpty(final @Nullable String str) {
    return str == null || str.trim().length() == 0;
  }

  public static boolean isPresent(final @Nullable String str) {
    return !isEmpty(str);
  }

  /**
   * Returns a string with only the first character capitalized.
   */
  public static @NonNull String sentenceCase(final @NonNull String str) {
    return str.length() <= 1 ? str.toUpperCase() : str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

  /**
   * Returns a string wrapped in parentheses.
   */
  public static @NonNull String wrapInParentheses(final @NonNull String str) {
    return "(" + str + ")";
  }
}
