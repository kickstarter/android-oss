package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtils {
  public final static int THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30;

  public static DateTimeFormatter defaultFormatter() {
    // Wrapper to make this easier to refactor later.
    return DateTimeFormat.forPattern("yyyy/MM/dd");
  }

  // i.e. August 20, 2015 at 7:45 PM.
  public static DateTimeFormatter writtenDeadline() {
    return DateTimeFormat.forPattern("MMMM dd, yyyy 'at' h:mm a.");
  }

  public static DateTimeFormatter estimatedDeliveryOn() {
    return DateTimeFormat.forPattern("MMMM yyyy");
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
}
