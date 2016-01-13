package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.util.Patterns;

public final class StringUtils {
  private StringUtils() {}

  public static boolean isEmail(final @NonNull CharSequence str) {
    return Patterns.EMAIL_ADDRESS.matcher(str).matches();
  }
}
