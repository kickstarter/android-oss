package com.kickstarter.libs.utils;

import android.content.Context;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RelativeDateTimeOptions;

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
    assertEquals("2015年12月", DateTimeUtils.estimatedDeliveryOn(DateTime.parse("2015-12-17T18:35:05Z"), Locale.JAPANESE));
  }

  @Test
  public void testFullDate() {
    assertEquals("Thursday, December 17, 2015", DateTimeUtils.fullDate(DateTime.parse("2015-12-17T18:35:05Z")));
    assertEquals("jeudi 17 décembre 2015", DateTimeUtils.fullDate(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH));
  }

  @Test
  public void testIsEpoch() {
    assertTrue(DateTimeUtils.isEpoch(DateTime.parse("1970-01-01T00:00:00Z")));
    assertTrue(DateTimeUtils.isEpoch(DateTime.parse("1969-12-31T19:00:00.000-05:00")));
    assertFalse(DateTimeUtils.isEpoch(DateTime.parse("2015-12-17T18:35:05Z")));
  }

  @Test
  public void testLongDate() {
    assertEquals("December 17, 2015", DateTimeUtils.longDate(DateTime.parse("2015-12-17T18:35:05Z")));
    assertEquals("17 décembre 2015", DateTimeUtils.longDate(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH));
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
  public void testRelative() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateTimeOptions.Builder builder = RelativeDateTimeOptions.builder();

    assertEquals("just now", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:10Z")).build()));
    assertEquals("right now", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:00Z")).build()));
    assertEquals("2 minutes ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()));
    assertEquals("in 2 minutes", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()));
    assertEquals("1 hour ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T19:35:05Z")).build()));
    assertEquals("in 1 hour", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T17:35:05Z")).build()));
    assertEquals("4 hours ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("in 4 hours", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
    assertEquals("23 hours ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-18T17:35:05Z")).build()));
    assertEquals("in 23 hours", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-16T19:35:05Z")).build()));
    assertEquals("yesterday", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-18T18:35:05Z")).build()));
    assertEquals("in 1 day", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-16T18:35:05Z")).build()));
    assertEquals("10 days ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-27T18:35:05Z")).build()));
    assertEquals("in 10 days", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-07T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2016-01-27T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-10-17T18:35:05Z")).build()));
  }

  @Test
  public void testRelative_withAbbreviated() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateTimeOptions.Builder builder = RelativeDateTimeOptions.builder().abbreviated(true);

    assertEquals("4 hrs ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("in 4 hrs", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
  }

  @Test
  public void testRelative_withAbsolute() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateTimeOptions.Builder builder = RelativeDateTimeOptions.builder().absolute(true);

    assertEquals("4 hours", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("4 hours", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
  }

  @Test
  public void testRelative_withThreshold() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final int threshold = 864_000; // Ten days
    final RelativeDateTimeOptions.Builder builder = RelativeDateTimeOptions.builder().threshold(threshold);

    assertEquals("9 days ago", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-26T18:35:05Z")).build()));
    assertEquals("in 9 days", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-08T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-28T18:35:05Z")).build()));
    assertEquals("Dec 17, 2015", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-06T18:35:05Z")).build()));
  }

  @Test
  @Config(qualifiers="de")
  public void testRelative_withLocale() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final RelativeDateTimeOptions.Builder builder = RelativeDateTimeOptions.builder();

    assertEquals("vor 2 Minuten", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()));
    assertEquals("in 2 Minuten", DateTimeUtils.relative(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()));
  }

  @Test
  public void testMediumDateShortTime() {
    assertEquals("Dec 17, 2015 6:35 PM", DateTimeUtils.mediumDateShortTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.UTC));
    assertEquals("Dec 17, 2015 1:35 PM", DateTimeUtils.mediumDateShortTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.forID("EST")));
    assertEquals("17 déc. 2015 18:35", DateTimeUtils.mediumDateShortTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.UTC, Locale.FRENCH));
  }

  @Test
  public void testShortTime() {
    assertEquals("6:35 PM", DateTimeUtils.shortTime(DateTime.parse("2015-12-17T18:35:05Z")));
    assertEquals("18:35", DateTimeUtils.shortTime(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH));
  }
}
