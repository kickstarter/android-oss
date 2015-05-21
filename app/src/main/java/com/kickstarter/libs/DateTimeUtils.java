package com.kickstarter.libs;

import org.joda.time.DateTime;
import org.joda.time.Period;

public class DateTimeUtils {
  public static int THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30;

  public static String relativeDateInWords(final DateTime date_time) {
    return relativeDateInWords(date_time, true, true, THIRTY_DAYS_IN_SECONDS);
  }

  public static String relativeDateInWords(final DateTime date_time, final boolean short_text) {
    return relativeDateInWords(date_time, short_text, true, THIRTY_DAYS_IN_SECONDS);
  }

  public static String relativeDateInWords(final DateTime date_time, final boolean short_text, final boolean explain) {
    return relativeDateInWords(date_time, short_text, explain, THIRTY_DAYS_IN_SECONDS);
  }

  public static String relativeDateInWords(final DateTime date_time,
    final boolean short_text,
    final boolean explain,
    final int threshold) {
    // TODO: This method is a quick translation from our iOS code, but it could use another pass.
    // There is probably some nice cleanup we can do using JodaTime.
    // TODO: Extract strings
    DateTime now = new DateTime();
    Period period = new Period(date_time, now);
    Integer seconds_difference = period.toStandardSeconds().getSeconds();
    Integer days_difference = period.toStandardDays().getDays();

    String ago_string = "";
    String in_string = "";
    if (seconds_difference < 0 && explain) {
      in_string += "in ";
    }
    if (seconds_difference > 0 && explain) {
      ago_string += " ago";
    }

    if (seconds_difference >= 0 && seconds_difference <= 60) {
      return "just now";
    } else if (seconds_difference >= -60 && seconds_difference <= 0) {
      return "right now";
    }

    seconds_difference = Math.abs(seconds_difference);
    days_difference = Math.abs(days_difference);

    if (seconds_difference < 3600) {
      int minutes_difference = (int) Math.floor(seconds_difference / 60.0);
      if (minutes_difference == 1) {
        return short_text ?
          in_string + "1 min" + ago_string :
          in_string + "1 minute" + ago_string;
      } else {
        return short_text ?
          minutes_difference + " mins" + ago_string :
          minutes_difference + " minutes" + ago_string;
      }
    } else if (seconds_difference < 86400) {
      int hours_difference = (int) Math.floor(seconds_difference / 60.0 / 60.0);
      if (hours_difference == 1) {
        return short_text ?
          in_string + "1 hr" + ago_string :
          in_string + "1 hour" + ago_string;
      } else {
        return short_text ?
          in_string + hours_difference + " hrs" + ago_string :
          in_string + hours_difference + " hours" + ago_string;
      }
    } else if (days_difference == 1) {
      return "yesterday";
    } else if (seconds_difference < threshold) {
      return in_string + days_difference + " days" + ago_string;
    } else {
      return date_time.toString();
    }
  }
}

/*
  else if (daysDiff == 1.0)
  {
    return _KS(@"dates.yesterday", @"yesterday");
  }
  else if (secondsDiff < threshold)
  {
    if (shortText)	return [NSString stringWithFormat:@"%@%.0f %@%@", inString, daysDiff, _KS(@"dates.units.days", @"days"), agoString];
    else			return [NSString stringWithFormat:@"%@%.0f %@%@", inString, daysDiff, _KS(@"dates.units.days", @"days"), agoString];
  }
  else
  {
    return [self formattedStringWithStyle:NSDateFormatterMediumStyle timeStyle:NSDateFormatterNoStyle];
  }

  return @"";
}
 */
