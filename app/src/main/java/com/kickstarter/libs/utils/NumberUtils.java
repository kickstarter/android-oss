package com.kickstarter.libs.utils;

import android.support.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {
  private NumberUtils(){}

  @Nullable
  public static String numberWithDelimiter(final Integer integer) {
    if (integer != null) {
      return NumberFormat.getNumberInstance(Locale.getDefault()).format(integer);
    }
    return null;
  }
}
