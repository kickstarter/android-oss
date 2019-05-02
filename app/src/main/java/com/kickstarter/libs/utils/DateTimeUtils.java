package com.kickstarter.libs.utils;

import android.content.Context;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.NumberOptions;
import com.kickstarter.libs.RelativeDateTimeOptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DateTimeUtils {
  private DateTimeUtils() {}

  public static final DateTime KSR10_BIRTHDAY = DateTime.parse("2019-04-30T00:01:00+00:00");
  public static final DateTime END_OF_BIRTHDAY_CELEBRATION = DateTime.parse("2019-05-14T23:59:59+00:00");

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
    return dateTime.toString(DateTimeFormat.forPattern(localePattern(locale)).withLocale(locale).withZoneUTC());
  }

  public static boolean isDateToday(final @NonNull DateTime dateTime) {
    return dateTime.withZone(DateTimeZone.UTC).withTimeAtStartOfDay()
      .equals(DateTime.now().withTimeAtStartOfDay().withZoneRetainFields(DateTimeZone.UTC));
  }

  /**
   * Returns a boolean indicating whether or not a DateTime value is the Epoch. Returns `true` if the
   * DateTime equals 1970-01-01T00:00:00Z.
   */
  public static boolean isEpoch(final @NonNull DateTime dateTime) {
    return dateTime.getMillis() == 0;
  }

  /**
   * Returns a boolean indicating whether or not a DateTime value is falls within the range of the KSR10 birthday
   * celebration.
   */
  public static boolean isWithinBirthdayCelebrationRange(final @NonNull DateTime dateTime) {
    final boolean isAfterBirthday = dateTime.isEqual(KSR10_BIRTHDAY) || dateTime.isAfter(KSR10_BIRTHDAY);
    final boolean isBeforeEndOfBirthdayCelebration = dateTime.isEqual(END_OF_BIRTHDAY_CELEBRATION)
      || dateTime.isBefore(END_OF_BIRTHDAY_CELEBRATION);
    return isAfterBirthday && isBeforeEndOfBirthdayCelebration;
  }

  /**
   * e.g.: Tuesday, June 20, 2017
   */
  public static @NonNull String fullDate(final @NonNull DateTime dateTime) {
    return fullDate(dateTime, Locale.getDefault());
  }

  /**
   * e.g.: Tuesday, June 20, 2017
   */
  public static @NonNull String fullDate(final @NonNull DateTime dateTime, final @NonNull Locale locale) {
    try {
      return dateTime.toString(DateTimeFormat.fullDate().withLocale(locale).withZoneUTC());
    } catch (final IllegalArgumentException e) {
      // JodaTime doesn't support the 'cccc' pattern, triggered by fullDate and fullDateTime. See: https://github.com/dlew/joda-time-android/issues/30
      // Instead just return a medium date.
      return mediumDate(dateTime, locale);
    }
  }

  /**
   * Returns the proper DateTime format pattern for supported locales.
   */
  private static @NonNull String localePattern(final @NonNull Locale locale) {
    switch(locale.getLanguage()) {
      case "de":
        return "MMMM yyyy";
      case "en":
        return "MMMM yyyy";
      case "es":
        return "MMMM yyyy";
      case "fr":
        return "MMMM yyyy";
      case "ja":
        return "yyyy'年'MMMM"; // NB Japanese in general should show year before month
      default:
        return "MMMM yyyy";
    }
  }

  /**
   * e.g.: June 20, 2017
   */
  public static @NonNull String longDate(final @NonNull DateTime dateTime) {
    return longDate(dateTime, Locale.getDefault());
  }

  /**
   * e.g.: June 20, 2017
   */
  public static @NonNull String longDate(final @NonNull DateTime dateTime, final @NonNull Locale locale) {
    return dateTime.toString(DateTimeFormat.longDate().withLocale(locale).withZoneUTC());
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
   * e.g.: Jan 14, 2016 2:20 PM.
   */
  public static @NonNull String mediumDateShortTime(final @NonNull DateTime dateTime) {
    return mediumDateShortTime(dateTime, DateTimeZone.getDefault(), Locale.getDefault());
  }

  /**
   * e.g.: Jan 14, 2016 2:20 PM.
   */
  public static @NonNull String mediumDateShortTime(final @NonNull DateTime dateTime, final @NonNull DateTimeZone dateTimeZone) {
    return mediumDateShortTime(dateTime, dateTimeZone, Locale.getDefault());
  }

  /**
   * e.g.: Jan 14, 2016 2:20 PM.
   */
  public static @NonNull String mediumDateShortTime(final @NonNull DateTime dateTime, final @NonNull DateTimeZone dateTimeZone,
    final @NonNull Locale locale) {
    final String mediumShortStyle = DateTimeFormat.patternForStyle("MS", locale);
    final DateTimeFormatter formatter = DateTimeFormat.forPattern(mediumShortStyle).withZone(dateTimeZone).withLocale(locale);
    return dateTime.toString(formatter);
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

    if (happenedAgo && "days".equals(unit) && difference == 1) {
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
   * e.g.: 4:20 PM
   */
  public static @NonNull String shortTime(final @NonNull DateTime dateTime) {
    return shortTime(dateTime, Locale.getDefault());
  }

  /**
   * e.g.: 4:20 PM
   */
  public static @NonNull String shortTime(final @NonNull DateTime dateTime, final @NonNull Locale locale) {
    return dateTime.toString(DateTimeFormat.shortTime().withLocale(locale).withZoneUTC());
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
