package com.kickstarter.libs.utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.functions.Func2;

public final class ListUtils {
  private ListUtils() {
    throw new AssertionError();
  }

  /**
   * Returns a new list with all elements in `xs` equal to `x` replaced by `newx`.
   */
  public static @NonNull <T> List<T> allReplaced(final @NonNull List<T> xs, final @NonNull T x, final @NonNull T newx) {
    final List<T> ys = new ArrayList<>(xs);

    for (int idx = 0; idx < xs.size(); idx++) {
      if (x.equals(xs.get(idx))) {
        ys.set(idx, newx);
      }
    }

    return ys;
  }

  /**
   * Appends `x` to the end of the list `xs`.
   */
  public static @NonNull <T> List<T> append(final @NonNull List<T> xs, final @NonNull T x) {
    final List<T> ys = new ArrayList<>(xs);
    ys.add(x);
    return ys;
  }

  /**
   * Concats the second argument onto the end of the first without mutating
   * either list.
   */
  public static <T> List<T> concat(final @NonNull List<T> xs, final @NonNull List<T> ys) {
    final List<T> zs = new ArrayList<>(xs);
    ListUtils.mutatingConcat(zs, ys);
    return zs;
  }

  /**
   * Concats the distinct elements of `ys` onto the end of the `xs` without mutating either list.
   */
  public static <T> List<T> concatDistinct(final @NonNull List<T> xs, final @NonNull List<T> ys) {
    final List<T> zs = new ArrayList<>(xs);
    ListUtils.mutatingConcatDistinct(zs, ys);
    return zs;
  }

  /**
   * Returns true if `y` is equal to any of the values in `xs`.
   */
  public static <T> boolean contains(final @NonNull List<T> xs, final @NonNull T y) {
    return ListUtils.contains(xs, y, Object::equals);
  }

  /**
   * Returns true if `y` is equal to any of the values in `xs`, where equality is determined by a given function.
   */
  public static <T> boolean contains(final @NonNull List<T> xs, final @NonNull T y, final @NonNull Func2<T, T, Boolean> equality) {
    return ListUtils.indexOf(xs, y, equality) != -1;
  }

  /**
   * Returns a list containing elements of `lhs` that do not exist in `rhs`.
   */
  public static @NonNull <T> List<T> difference(final @NonNull List<T> lhs, final @NonNull List<T> rhs) {
    return ListUtils.difference(lhs, rhs, Object::equals);
  }

  /**
   * Returns a list containing elements of `lhs` that do not exist in `rhs`, where equality between elements is
   * determined by a given function.
   */
  public static @NonNull <T> List<T> difference(final @NonNull List<T> lhs, final @NonNull List<T> rhs,
    final @NonNull Func2<T, T, Boolean> equality) {

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

  /**
   * Returns the first element in `xs` that equals `x`, or `null` if `x` is not found in `xs`.
   */
  public static @Nullable <T> T find(final @NonNull List<T> xs, final @NonNull T x) {
    return ListUtils.find(xs, x, Object::equals);
  }

  /**
   * Returns the first element in `xs` that equals `x`, or `null` if `x` is not found in `xs`. Equality is determined
   * by the given function.
   */
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
  public static @Nullable <T> T first(final @NonNull List<T> xs) {
    return xs.size() > 0 ? xs.get(0) : null;
  }

  /**
   * Combines a list of lists into a new single, flat list.
   */
  public static @NonNull <T> List<T> flatten(final @NonNull List<List<T>> xss) {
    final List<T> result = new ArrayList<>();
    for (final List<T> xs : xss) {
      result.addAll(xs);
    }
    return result;
  }

  /**
   * Returns the index of the first element in `xs` that equals `x`, or `-1` if `x` is not found in `xs`.
   */
  public static <T> int indexOf(final @NonNull List<T> xs, final @NonNull T y) {
    return ListUtils.indexOf(xs, y, Object::equals);
  }

  /**
   * Returns the index of the first element in `xs` that equals `x`, or `-1` if `x` is not found in `xs`. Equality is determined
   * by the given function.
   */
  public static <T> int indexOf(final @NonNull List<T> xs, final @NonNull T y, final @NonNull Func2<T, T, Boolean> equality) {
    for (int idx = 0; idx < xs.size(); idx++) {
      if (equality.call(xs.get(idx), y)) {
        return idx;
      }
    }
    return -1;
  }

  /**
   * Returns a list containing the elements of `lhs` that exist in `rhs`.
   */
  public static @NonNull <T> List<T> intersection(final @NonNull List<T> lhs, final @NonNull List<T> rhs) {
    return ListUtils.intersection(lhs, rhs, Object::equals);
  }

  /**
   * Returns a list containing the elements of `lhs` that exist in `rhs`, where equality is determined by the given function.
   */
  public static @NonNull <T> List<T> intersection(final @NonNull List<T> lhs, final @NonNull List<T> rhs, final @NonNull Func2<T, T, Boolean> equality) {
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
  public static <T> List<T> mutatingConcat(final @NonNull List<T> xs, final @NonNull List<T> ys) {
    xs.addAll(ys);
    return xs;
  }

  /**
   * Concats the distinct elements of `ys` onto the end of the `xs`, but also mutates the first.
   */
  public static <T> List<T> mutatingConcatDistinct(final @NonNull List<T> xs, final @NonNull List<T> ys) {
    for (final T y : ys) {
      if (!xs.contains(y)) {
        xs.add(y);
      }
    }
    return xs;
  }

  /**
   * Checks the size of a list and returns `true` if the list is non empty.
   */
  public static <T> boolean nonEmpty(final @NonNull List<T> xs) {
    return xs.size() > 0;
  }

  /**
   * Prepends `x` to the beginning of the list `xs`.
   */
  public static <T> List<T> prepend(final @NonNull List<T> xs, final @NonNull T x) {
    final List<T> ys = new ArrayList<>(xs);
    ys.add(0, x);
    return ys;
  }

  /**
   * Replaces the element at index `idx` with the element `x`. Does so by return a whole new list without
   * mutating the original.
   */
  public static @NonNull <T> List<T> replaced(final @NonNull List<T> xs, final int idx, final @Nullable T x) {
    final List<T> ys = new ArrayList<>(xs);
    ys.set(idx, x);
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
