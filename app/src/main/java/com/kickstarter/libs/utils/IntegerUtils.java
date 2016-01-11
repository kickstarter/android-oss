package com.kickstarter.libs.utils;

import android.support.annotation.Nullable;

public final class IntegerUtils {
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
}
