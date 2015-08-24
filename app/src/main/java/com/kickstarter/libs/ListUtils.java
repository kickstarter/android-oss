package com.kickstarter.libs;

import java.util.List;

public class ListUtils {
  private ListUtils() {
    throw new AssertionError();
  }

  /**
   * Concats the second argument onto the end of the first, but also mutates the
   * first argument.
   */
  public static <T> List<T> mutatingConcat(final List<T> xs, final List<T> ys) {
    xs.addAll(ys);
    return xs;
  }
}
