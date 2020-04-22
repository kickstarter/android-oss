package com.kickstarter.libs.utils;

import androidx.annotation.Nullable;

public final class IntegerUtils {
  private IntegerUtils() {}

  /**
   * Returns `false` if `value` is `null` or `0`, and `true` otherwise.
   */
  public static boolean isNonZero(final @Nullable Integer value) {
    return value != null && value != 0;
  }

  /**
   * Returns `true` if `value` is zero, and false otherwise, including when `value` is `null`.
   */
  public static boolean isZero(final @Nullable Integer value) {
    return value != null && value == 0;
  }

  /**
   * Returns `value` if not null, and `0` otherwise.
   */
  public static int intValueOrZero(final @Nullable Integer value) {
    return value != null ? value : 0;
  }

  /**
   * Returns `true` if `value` is null or zero, and false otherwise.
   */
  public static boolean isNullOrZero(final @Nullable Integer value) {
    return value == null || value == 0;
  }

  /**
   * Returns `true` if `value` is zero or higher, and false otherwise.
   */
  public static boolean isZeroOrHigher(final @Nullable Integer value) {
    return value != null && value >= 0;
  }
}
