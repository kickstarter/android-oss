package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func2;

public class ListUtils {
  private ListUtils() {
    throw new AssertionError();
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
   * Concats the second argument onto the end of the first without mutating
   * either list.
   */
  public static <T> List<T> concat(@NonNull final List<T> xs, @NonNull final List<T> ys) {
    final List<T> zs = new ArrayList<>(xs);
    ListUtils.mutatingConcat(zs, ys);
    return zs;
  }

  /**
   * Concats the distinct elements of `ys` onto the end of the `xs` without mutating either list.
   */
  public static <T> List<T> concatDistinct(@NonNull final List<T> xs, @NonNull final List<T> ys) {
    final List<T> zs = new ArrayList<>(xs);
    ListUtils.mutatingConcatDistinct(zs, ys);
    return zs;
  }

  public static <T> boolean contains(final @NonNull List<T> xs, final @NonNull T y) {
    return ListUtils.contains(xs, y, Object::equals);
  }

  public static <T> boolean contains(final @NonNull List<T> xs, final @NonNull T y, final @NonNull Func2<T, T, Boolean> equality) {
    return ListUtils.indexOf(xs, y, equality) != -1;
  }

  public static @NonNull <T> List<T> difference(final @NonNull List<T> lhs, final @NonNull List<T> rhs) {
    return ListUtils.difference(lhs, rhs, Object::equals);
  }

  public static @NonNull <T> List<T> difference(final @NonNull List<T> lhs, final @NonNull List<T> rhs, Func2<T, T, Boolean> equality) {
    final List<T> result = new ArrayList<>();
    for (final T litem : lhs) {
      if (!ListUtils.contains(rhs, litem, equality)) {
        result.add(litem);
      }
    }
    return result;
  }

  /**
   * Returns an empty list.
   */
  @NonNull public static <T> List<T> empty() {
    return new ArrayList<>();
  }

  public static @Nullable <T> T find(final @NonNull List<T> xs, final @NonNull T x) {
    return ListUtils.find(xs, x, Object::equals);
  }

  public static @Nullable <T> T find(final @NonNull List<T> xs, final @NonNull T y, final @NonNull Func2<T, T, Boolean> equality) {
    final int idx = ListUtils.indexOf(xs, y, equality);
    if (idx == -1) {
      return null;
    }
    return xs.get(idx);
  }

  /**
   * Returns the first object or `null` if empty.
   */
  @Nullable public static <T> T first(@NonNull final List<T> xs) {
    return xs.size() > 0 ? xs.get(0) : null;
  }

  public static @NonNull <T> List<T> flatten(final @NonNull List<List<T>> xss) {
    final List<T> result = new ArrayList<>();
    for (final List<T> xs : xss) {
      result.addAll(xs);
    }
    return result;
  }

  public static <T> int indexOf(final @NonNull List<T> xs, final @NonNull T x) {
    return ListUtils.indexOf(xs, x, Object::equals);
  }

  public static <T> int indexOf(final @NonNull List<T> xs, final @NonNull T y, final @NonNull Func2<T, T, Boolean> equality) {
    for (int idx = 0; idx < xs.size(); idx++) {
      if (equality.call(xs.get(idx), y)) {
        return idx;
      }
    }
    return -1;
  }

  public static @NonNull <T> List<T> intersection(final @NonNull List<T> lhs, final @NonNull List<T> rhs) {
    return ListUtils.intersection(lhs, rhs, Object::equals);
  }

  public static @NonNull <T> List<T> intersection(final @NonNull List<T> lhs, final @NonNull List<T> rhs, Func2<T, T, Boolean> equality) {
    final List<T> result = new ArrayList<>();
    for (final T litem : lhs) {
      if (ListUtils.contains(rhs, litem, equality)) {
        result.add(litem);
      }
    }
    return result;
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
   * Prepends `x` to the beginning of the list `xs`.
   */
  public static <T> List<T> prepend(@NonNull final List<T> xs, @NonNull final T x) {
    final List<T> ys = new ArrayList<>(xs);
    ys.add(0, x);
    return ys;
  }

  public static @NonNull <T> List<T> replace(final @NonNull List<T> xs, final @NonNull T x, final @NonNull T newx) {
    final List<T> ys = new ArrayList<>(xs);

    for (int idx = 0; idx < xs.size(); idx++) {
      if (x.equals(xs.get(idx))) {
        ys.set(idx, newx);
      }
    }

    return ys;
  }

  /**
   * Uses Fisher-Yates algorithm to shuffle an array without mutating input arg.
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
