package com.kickstarter.libs;

import android.util.Patterns;

public class StringUtils {
  private StringUtils() {}

  public static boolean isEmail(final CharSequence str) {
    return Patterns.EMAIL_ADDRESS.matcher(str).matches();
  }
}
