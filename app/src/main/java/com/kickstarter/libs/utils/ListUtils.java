package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
  private ListUtils() {
    throw new AssertionError();
  }

  /**
   * Returns an empty list.
   */
  @NonNull public static <T> List<T> empty() {
    return new ArrayList<>();
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
   * Concats the distinct elements of `ys` onto the end of the `xs`, but also mutates the first.
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
   * Concats the distinct elements of `ys` onto the end of the `xs` without mutating either list.
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

  /**
   * Returns the first object or `null` if empty.
   */
  @Nullable public static <T> T first(@NonNull final List<T> xs) {
    return xs.size() > 0 ? xs.get(0) : null;
  }

  /**
   * Uses Fisher-Yates algorithm to shuffle an array.
   * http://www.dotnetperls.com/shuffle-java
   */
  public static <T> List<T> shuffle(final @NonNull List<T> xs) {
    final List<T> ys = new ArrayList<>(xs);
    final int length = ys.size();

    for (int i = 0; i < length; i++) {
      final int j = i + (int) (Math.random() * (length - i));
      final T temp = ys.get(i);
      ys.set(i, ys.get(j));
      ys.set(j, temp);
    }

    return ys;
  }
}
