package com.kickstarter.libs.utils;


import java.util.Comparator;

import androidx.annotation.NonNull;

public final class ComparatorUtils {

  public static final class DescendingOrderFloatComparator implements Comparator<Float> {
    @Override
    public int compare(final @NonNull Float x, final @NonNull Float y) {
      return (x < y) ? 1 : ((x.floatValue() == y.floatValue()) ? 0 : -1);
    }
  }

  public static final class DescendingOrderIntegerComparator implements Comparator<Integer> {
    /*
    * IMPORTANT: this sorts in descending order.
     */
    @Override
    public int compare(final @NonNull Integer x, final @NonNull Integer y) {
      return (x < y) ? 1 : ((x.intValue() == y.intValue()) ? 0 : -1);
    }
  }
}
