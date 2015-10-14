package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
  private ListUtils() {
    throw new AssertionError();
  }

  /**
   * Concats the second argument onto the end of the first, but also mutates the
   * first argument.
   */
  public static <T> List<T> mutatingConcat(@NonNull final List<T> xs, @NonNull final List<T> ys) {
    xs.addAll(ys);
    return xs;
  }

  /**
   * Concats the second argument onto the end of the first without mutating
   * either list.
   */
  public static <T> List<T> concat(@NonNull final List<T> xs, @NonNull final List<T> ys) {
    final List<T> zs = new ArrayList<>(xs);
    ListUtils.mutatingConcat(zs, ys);
    return zs;
  }

  /**
   * Concats the distinct elements of the second argument onto the end of the
   * first, but also mutates the first.
   */
  public static <T> List<T> mutatingConcatDistinct(@NonNull final List<T> xs, @NonNull final List<T> ys) {
    for (final T y : ys) {
      if (! xs.contains(y)) {
        xs.add(y);
      }
    }
    return xs;
  }

  /**
   * Concats the distinct elements of the second argument onto the end of the
   * first without mutating either list.
   */
  public static <T> List<T> concatDistinct(@NonNull final List<T> xs, @NonNull final List<T> ys) {
    final List<T> zs = new ArrayList<>(xs);
    ListUtils.mutatingConcatDistinct(zs, ys);
    return zs;
  }

  /**
   * Prepends `x` to the beginning of the list `xs`.
   */
  public static <T> List<T> prepend(@NonNull final List<T> xs, @NonNull final T x) {
    final List<T> ys = new ArrayList<>(xs);
    ys.add(0, x);
    return ys;
  }

  /**
   * Appends `x` to the end of the list `xs`.
   */
  public static <T> List<T> append(@NonNull final List<T> xs, @NonNull final T x) {
    final List<T> ys = new ArrayList<>(xs);
    ys.add(x);
    return ys;
  }
}
