package com.kickstarter.libs.utils;

import android.content.Context;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RelativeDateOptions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.Locale;

public final class DateTimeUtilsTest extends KSRobolectricTestCase {
  @Test
  public void testEstimatedDeliveryOn() {
    assertEquals("December 2015", DateTimeUtils.estimatedDeliveryOn(DateTime.parse("2015-12-17T18:35:05Z")));
    assertEquals("décembre 2015", DateTimeUtils.estimatedDeliveryOn(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH));
  }

  @Test
  public void testMediumDate() {
    assertEquals("Dec 17, 2015", DateTimeUtils.mediumDate(DateTime.parse("2015-12-17T18:35:05Z")));
    assertEquals("17 déc. 2015", DateTimeUtils.mediumDate(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH));
  }

  @Test
  public void testMediumDateTime() {
    assertEquals("Dec 17, 2015 6:35:05 PM", DateTimeUtils.mediumDateTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.UTC));
    assertEquals("Dec 17, 2015 1:35:05 PM", DateTimeUtils.mediumDateTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.forID("EST")));
    assertEquals("17 déc. 2015 18:35:05", DateTimeUtils.mediumDateTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.UTC, Locale.FRENCH));
  }

  @Test
  public void testRelativeDate() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateOptions.Builder builder = RelativeDateOptions.builder();

    assertEquals("just now", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:10Z")).build()));
    assertEquals("right now", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:00Z")).build()));
    assertEquals("2 minutes ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()));
    assertEquals("in 2 minutes", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()));
    assertEquals("1 hour ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T19:35:05Z")).build()));
    assertEquals("in 1 hour", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T17:35:05Z")).build()));
    assertEquals("4 hours ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("in 4 hours", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
    assertEquals("23 hours ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-18T17:35:05Z")).build()));
    assertEquals("in 23 hours", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-16T19:35:05Z")).build()));
    assertEquals("yesterday", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-18T18:35:05Z")).build()));
    assertEquals("in 1 day", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-16T18:35:05Z")).build()));
    assertEquals("10 days ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-27T18:35:05Z")).build()));
    assertEquals("in 10 days", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-07T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2016-01-27T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-10-17T18:35:05Z")).build()));
  }

  @Test
  public void testRelativeDate_withAbbreviated() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateOptions.Builder builder = RelativeDateOptions.builder().abbreviated(true);

    assertEquals("4 hrs ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("in 4 hrs", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
  }

  @Test
  public void testRelativeDate_withAbsolute() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateOptions.Builder builder = RelativeDateOptions.builder().absolute(true);

    assertEquals("4 hours", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("4 hours", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
  }

  @Test
  public void testRelativeDate_withThreshold() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final int threshold = 864_000; // Ten days
    final RelativeDateOptions.Builder builder = RelativeDateOptions.builder().threshold(threshold);

    assertEquals("9 days ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-26T18:35:05Z")).build()));
    assertEquals("in 9 days", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-08T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-28T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-06T18:35:05Z")).build()));
  }

  @Test
  @Config(qualifiers="de")
  public void testRelativeDate_withLocale() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateOptions.Builder builder = RelativeDateOptions.builder();

    assertEquals("vor 2 Minuten", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()));
    assertEquals("in 2 Minuten", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()));
  }
}
