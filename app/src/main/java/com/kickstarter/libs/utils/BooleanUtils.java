package com.kickstarter.libs.utils;

import android.support.annotation.Nullable;

public final class BooleanUtils {

  /**
   * Returns `false` if the boolean is `null` or `false`, and `true` otherwise.
   */
  public static boolean isTrue(final @Nullable Boolean bool) {
    if (bool != null) {
      return bool;
    }
    return false;
  }
}
