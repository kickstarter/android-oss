package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.NumberOptions;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class NumberUtils {
  private NumberUtils() {}

  public static @Nullable String numberWithDelimiter(final @Nullable Integer integer) {
    if (integer != null) {
      return NumberFormat.getNumberInstance(Locale.getDefault()).format(integer);
    }
    return null;
  }

  public static @NonNull String formatNumber(final float value) {
    return formatNumber(value, NumberOptions.builder().build());
  }

  public static @NonNull String formatNumber(final float value, final @NonNull NumberOptions options) {
    NumberFormat numberFormat = NumberFormat.getInstance();

    if (numberFormat instanceof DecimalFormat) {
      numberFormat.setRoundingMode(RoundingMode.HALF_DOWN);
    }

    int precision = ObjectUtils.coalesce(options.precision(), 0);
    float divisor = 1.0f;

    final String prefix = options.currencySymbol() != null ? options.currencySymbol() : "";

    String suffix = "";
    final float bucketAbove = ObjectUtils.coalesce(options.bucketAbove(), 0.0f);
    if (bucketAbove >= 1000.0f && value >= bucketAbove) {
      if (bucketAbove > 0.0f && bucketAbove < 1000000.0f) {
        divisor = 1000.0f;
        suffix = "K";
      } else if (bucketAbove >= 1000000.0f) {
        divisor = 1000000.0f;
        suffix = "M";
      }
      if (options.bucketAbove() != null) {
        precision = ObjectUtils.coalesce(options.bucketPrecision(), 0);
      }
    }

    if (options.currencyCode() != null) {
      suffix = String.format("%s %s", suffix, options.currencyCode());
    }

    numberFormat.setMinimumFractionDigits(precision);
    numberFormat.setMaximumFractionDigits(precision);

    float bucketedValue = value;
    if (value >= bucketAbove) {
      bucketedValue = value / divisor;
    }

    return String.format("%s%s%s", prefix, numberFormat.format(bucketedValue), suffix);
  }
}
