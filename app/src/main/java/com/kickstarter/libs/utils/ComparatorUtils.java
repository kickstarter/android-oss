package com.kickstarter.libs.utils;


import android.support.annotation.NonNull;

import java.util.Comparator;

public final class ComparatorUtils {

  public static final class IntegerComparator implements Comparator<Integer> {
    /*
    * IMPORTANT: this sorts in descending order.
     */
    @Override
    public int compare(final @NonNull Integer x,final @NonNull Integer y) {
      return (x < y) ? 1 : ((x.intValue() == y.intValue()) ? 0 : -1);
    }
  }
}
