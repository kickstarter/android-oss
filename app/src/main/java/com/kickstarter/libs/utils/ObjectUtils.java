package com.kickstarter.libs.utils;

import android.support.annotation.Nullable;

public class ObjectUtils {
  private ObjectUtils(){}

  public static boolean isNull(@Nullable final Object object) {
    return object == null;
  }

  public static boolean isNotNull(@Nullable final Object object) {
    return object != null;
  }
}
