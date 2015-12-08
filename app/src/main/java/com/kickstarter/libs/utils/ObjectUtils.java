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

  /**
   * Converts an {@link Integer} to a {@link String}, or null of the integer is also null.
   */
  public static @Nullable String toString(@Nullable final Integer n) {
    if (n != null) {
      return Integer.toString(n);
    }

    return null;
  }

  /**
   * Converts a {@link Long} to a {@link String}, or null of the long is also null.
   */
  public static @Nullable String toString(@Nullable final Long n) {
    if (n != null) {
      return Long.toString(n);
    }

    return null;
  }

  /**
   * Converts a {@link Float} to a {@link String}, or null of the float is also null.
   */
  public static @Nullable String toString(@Nullable final Float n) {
    if (n != null) {
      return Float.toString(n);
    }

    return null;
  }

  /**
   * Converts a {@link Double} to a {@link String}, or null of the double is also null.
   */
  public static @Nullable String toString(@Nullable final Double n) {
    if (n != null) {
      return Double.toString(n);
    }

    return null;
  }
}
