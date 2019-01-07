package com.kickstarter.libs.utils;

import android.util.Pair;

import androidx.annotation.NonNull;

public final class PairUtils {
  private PairUtils() {}

  /**
   * Returns the first of any input pair
   */
  public static <R> R first(final @NonNull Pair<R, ?> anyPair) {
    return anyPair.first;
  }

  /**
   * Returns the second of any input pair
   */
  public static <R> R second(final @NonNull Pair<?, R> anyPair) {
    return anyPair.second;
  }
}
