package com.kickstarter.libs.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.NumberOptions;
import com.kickstarter.libs.RelativeDateTimeOptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

public final class DateTimeUtils {
  private DateTimeUtils() {}

  /**
   * e.g.: December 2015.
   */
  public static @NonNull String estimatedDeliveryOn(final @NonNull DateTime dateTime) {
    return estimatedDeliveryOn(dateTime, Locale.getDefault());
  }

  /**
   * e.g.: December 2015.
   */
  public static @NonNull String estimatedDeliveryOn(final @NonNull DateTime dateTime, final @NonNull Locale locale) {
    return dateTime.toString(DateTimeFormat.forPattern("MMMM yyyy").withLocale(locale).withZoneUTC());
  }

  public static boolean isDateToday(final @NonNull DateTime dateTime) {
    return dateTime.withZone(DateTimeZone.UTC).withTimeAtStartOfDay()
      .equals(DateTime.now().withTimeAtStartOfDay().withZoneRetainFields(DateTimeZone.UTC));
  }

  /**
   * e.g.: Dec 17, 2015.
   */
  public static @NonNull String fullDate(final @NonNull DateTime dateTime) {
    return fullDate(dateTime, Locale.getDefault());
  }

  /**
   * e.g.: Dec 17, 2015.
   */
  public static @NonNull String fullDate(final @NonNull DateTime dateTime, final @NonNull Locale locale) {
    return dateTime.toString(DateTimeFormat.fullDate().withLocale(locale).withZoneUTC());
  }

  /**
   * e.g.: Dec 17, 2015.
   */
  public static @NonNull String mediumDate(final @NonNull DateTime dateTime) {
    return mediumDate(dateTime, Locale.getDefault());
  }

  /**
   * e.g.: Dec 17, 2015.
   */
  public static @NonNull String mediumDate(final @NonNull DateTime dateTime, final @NonNull Locale locale) {
    return dateTime.toString(DateTimeFormat.mediumDate().withLocale(locale).withZoneUTC());
  }

  /**
   * e.g.: Dec 17, 2015 6:35:05 PM.
   */
  public static @NonNull String mediumDateTime(final @NonNull DateTime dateTime) {
    return mediumDateTime(dateTime, DateTimeZone.getDefault());
  }

  /**
   * e.g.: Dec 17, 2015 6:35:05 PM.
   */
  public static @NonNull String mediumDateTime(final @NonNull DateTime dateTime, final @NonNull DateTimeZone dateTimeZone) {
    return mediumDateTime(dateTime, dateTimeZone, Locale.getDefault());
  }

  /**
   * e.g.: Dec 17, 2015 6:35:05 PM.
   */
  public static @NonNull String mediumDateTime(final @NonNull DateTime dateTime, final @NonNull DateTimeZone dateTimeZone,
    final @NonNull Locale locale) {
    return dateTime.toString(DateTimeFormat.mediumDateTime().withLocale(locale).withZone(dateTimeZone));
  }

  /**
   * Returns a string indicating the distance between {@link DateTime}s. Defaults to comparing the input {@link DateTime} to
   * the current time.
   */
  public static @NonNull String relative(final @NonNull Context context, final @NonNull KSString ksString,
    final @NonNull DateTime dateTime) {
    return relative(context, ksString, dateTime, RelativeDateTimeOptions.builder().build());
  }

  /**
   * Returns a string indicating the distance between {@link DateTime}s. Defaults to comparing the input {@link DateTime} to
   * the current time.
   */
  public static @NonNull String relative(final @NonNull Context context, final @NonNull KSString ksString,
    final @NonNull DateTime dateTime, final @NonNull RelativeDateTimeOptions options) {

    final DateTime relativeToDateTime = ObjectUtils.coalesce(options.relativeToDateTime(), DateTime.now());
    final Seconds seconds = Seconds.secondsBetween(dateTime, relativeToDateTime);
    final int secondsDifference = seconds.getSeconds();

    if (secondsDifference >= 0.0 && secondsDifference <= 60.0) {
      return context.getString(R.string.dates_just_now);
    } else if (secondsDifference >= -60.0 && secondsDifference <= 0.0) {
      return context.getString(R.string.dates_right_now);
    }

    final Pair<String, Integer> unitAndDifference = unitAndDifference(secondsDifference, options.threshold());
    if (unitAndDifference == null) {
      // Couldn't find a good match, just render the date.
      return mediumDate(dateTime);
    }

    final String unit = unitAndDifference.first;
    final int difference = unitAndDifference.second;
    boolean willHappenIn = false;
    boolean happenedAgo = false;

    if (!options.absolute()) {
      if (secondsDifference < 0) {
        willHappenIn = true;
      } else if (secondsDifference > 0) {
        happenedAgo = true;
      }
    }

    if (happenedAgo && unit.equals("days") && difference == 1) {
      return context.getString(R.string.dates_yesterday);
    }

    final StringBuilder baseKeyPath = new StringBuilder();
    if (willHappenIn) {
      baseKeyPath.append(String.format("dates_time_in_%s", unit));
    } else if (happenedAgo) {
      baseKeyPath.append(String.format("dates_time_%s_ago", unit));
    } else {
      baseKeyPath.append(String.format("dates_time_%s", unit));
    }

    if (options.abbreviated()) {
      baseKeyPath.append("_abbreviated");
    }

    return ksString.format(baseKeyPath.toString(), difference,
      "time_count", NumberUtils.format(difference, NumberOptions.builder().build()));
  }

  /**
   * Utility to pair a unit (e.g. "minutes", "hours", "days") with a measurement. Returns `null` if the difference
   * exceeds the threshold.
   */
  private static @Nullable Pair<String, Integer> unitAndDifference(final int initialSecondsDifference, final int threshold) {
    final int secondsDifference = Math.abs(initialSecondsDifference);
    final int daysDifference = (int) Math.floor(secondsDifference / 86400);

    if (secondsDifference < 3600) { // 1 hour
      final int minutesDifference = (int) Math.floor(secondsDifference / 60.0);
      return new Pair<>("minutes", minutesDifference);
    } else if (secondsDifference < 86400) { // 24 hours
      final int hoursDifference = (int) Math.floor(secondsDifference / 60.0 / 60.0);
      return new Pair<>("hours", hoursDifference);
    } else if (secondsDifference < threshold) {
      return new Pair<>("days", daysDifference);
    }

    return null;
  }
}
