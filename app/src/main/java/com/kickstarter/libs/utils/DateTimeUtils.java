package com.kickstarter.libs.utils;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import auto.parcel.AutoParcel;

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

  public static @NonNull String relativeDate(final @NonNull Context context, final @NonNull KSString ksString,
    final @NonNull DateTime dateTime) {
    return relativeDate(context, ksString, dateTime, RelativeDateOptions.builder().build());
  }

  public static @NonNull String relativeDate(final @NonNull Context context, final @NonNull KSString ksString,
    final @NonNull DateTime dateTime, final @NonNull RelativeDateOptions options) {
    return relativeDate(context, ksString, dateTime, options, Locale.getDefault());
  }

  public static @NonNull String relativeDate(final @NonNull Context context, final @NonNull KSString ksString,
    final @NonNull DateTime dateTime, final @NonNull RelativeDateOptions options, final @NonNull Locale locale) {

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
      return mediumDate(dateTime, locale);
    }

    final String unit = unitAndDifference.first;
    final int difference = unitAndDifference.second;
    boolean willHappenIn = false;
    boolean happenedAgo = false;

    if (options.explain()) {
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

    if (options.shorten()) {
      baseKeyPath.append("_abbreviated");
    }

    return ksString.format(baseKeyPath.toString(), difference,
      "time_count", String.valueOf(difference));
  }

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

  public static DateTimeFormatter defaultFormatter() {
    // Wrapper to make this easier to refactor later.
    return DateTimeFormat.forPattern("yyyy/MM/dd");
  }

  // e.g. August 20, 2015 at 7:45 PM.
  public static DateTimeFormatter writtenDeadline() {
    return DateTimeFormat.forPattern("MMMM dd, yyyy 'at' h:mm a.");
  }

  // e.g. Wednesday, September 23, 2015
  public static DateTimeFormatter pledgedAt() {
    return DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy");
  }

  public static DateTimeFormatter estimatedDeliveryOn() {
    return DateTimeFormat.forPattern("MMMM yyyy");
  }

  public static boolean isDateToday(final @NonNull DateTime dateTime) {
    return dateTime.withZone(DateTimeZone.UTC).withTimeAtStartOfDay()
      .equals(DateTime.now().withTimeAtStartOfDay().withZoneRetainFields(DateTimeZone.UTC));
  }

  public static String relativeDateInWords(@NonNull final DateTime dateTime) {
    return relativeDateInWords(dateTime, true, true, THIRTY_DAYS_IN_SECONDS);
  }

  public static String relativeDateInWords(@NonNull final DateTime dateTime, final boolean shortText) {
    return relativeDateInWords(dateTime, shortText, true, THIRTY_DAYS_IN_SECONDS);
  }

  public static String relativeDateInWords(@NonNull final DateTime dateTime, final boolean shortText, final boolean explain) {
    return relativeDateInWords(dateTime, shortText, explain, THIRTY_DAYS_IN_SECONDS);
  }

  public static String relativeDateInWords(@NonNull final DateTime dateTime, final boolean shortText,
    final boolean explain, final int threshold) {
    // TODO: This method is a quick translation from our iOS code, but it needs another pass, e.g.: we should
    // extract these strings, look into JodaTime to see if we can clean anything up..
    final DateTime now = new DateTime();
    final Seconds seconds = Seconds.secondsBetween(dateTime, now);
    Integer secondsDifference = seconds.getSeconds();
    Integer daysDifference = seconds.toStandardDays().getDays();

    String agoString = "";
    String inString = "";
    if (secondsDifference < 0 && explain) {
      inString += "in ";
    }
    if (secondsDifference > 0 && explain) {
      agoString += " ago";
    }

    if (secondsDifference >= 0 && secondsDifference <= 60) {
      return "just now";
    } else if (secondsDifference >= -60 && secondsDifference <= 0) {
      return "right now";
    }

    secondsDifference = Math.abs(secondsDifference);
    daysDifference = Math.abs(daysDifference);

    if (secondsDifference < 3600) {
      int minutesDifference = (int) Math.floor(secondsDifference / 60.0);
      if (minutesDifference == 1) {
        return shortText ?
          inString + "1 min" + agoString :
          inString + "1 minute" + agoString;
      } else {
        return shortText ?
          minutesDifference + " mins" + agoString :
          minutesDifference + " minutes" + agoString;
      }
    } else if (secondsDifference < 86400) {
      int hoursDifference = (int) Math.floor(secondsDifference / 60.0 / 60.0);
      if (hoursDifference == 1) {
        return shortText ?
          inString + "1 hr" + agoString :
          inString + "1 hour" + agoString;
      } else {
        return shortText ?
          inString + hoursDifference + " hrs" + agoString :
          inString + hoursDifference + " hours" + agoString;
      }
    } else if (daysDifference == 1) {
      return "yesterday";
    } else if (secondsDifference < threshold) {
      return inString + daysDifference + " days" + agoString;
    } else {
      return dateTime.toString(defaultFormatter());
    }
  }

  @AutoParcel
  public abstract static class RelativeDateOptions implements Parcelable {
    public abstract boolean explain();
    public abstract boolean shorten();
    public abstract @Nullable DateTime relativeToDateTime();
    public abstract int threshold();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder explain(boolean __);
      public abstract Builder shorten(boolean __);
      public abstract Builder relativeToDateTime(DateTime __);
      public abstract Builder threshold(int __);
      public abstract RelativeDateOptions build();
    }

    public static Builder builder() {
      return new AutoParcel_DateTimeUtils_RelativeDateOptions.Builder()
        .explain(true)
        .shorten(true)
        .threshold(THIRTY_DAYS_IN_SECONDS);
    }

    public abstract Builder toBuilder();
  }

  private final static int THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30;
}
