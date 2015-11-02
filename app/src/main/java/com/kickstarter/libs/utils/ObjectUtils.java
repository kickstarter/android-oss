package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.functions.Func1;

public class ObjectUtils {
  private ObjectUtils(){}

  public static boolean isNull(@Nullable final Object object) {
    return object == null;
  }

  public static boolean isNotNull(@Nullable final Object object) {
    return object != null;
  }

  /**
   * Returns the first non-`null` value of its arguments.
   */
  @NonNull public static <T> T coalesce(@Nullable final T value, @NonNull final T theDefault) {
    if (value != null) {
      return value;
    }
    return theDefault;
  }

  /**
   * Returns a function `T -> T` that coalesces values with `theDefault`.
   */
  @NonNull public static <T> Func1<T, T> coalesceWith(@NonNull final T theDefault) {
    return (value) -> ObjectUtils.coalesce(value, theDefault);
  }
}
