package com.kickstarter.libs.utils;

import android.content.Context;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.KSString;

import org.joda.time.DateTime;
import org.junit.Test;

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
  public void testRelativeDate() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final DateTimeUtils.RelativeDateOptions.Builder builder = DateTimeUtils.RelativeDateOptions.builder();

    assertEquals("just now", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:10Z")).build()));
    assertEquals("right now", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:00Z")).build()));
    assertEquals("2 mins ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()));
    assertEquals("in 2 mins", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()));
    assertEquals("1 hr ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T19:35:05Z")).build()));
    assertEquals("in 1 hr", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T17:35:05Z")).build()));
    assertEquals("4 hrs ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("in 4 hrs", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
    assertEquals("23 hrs ago", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-18T17:35:05Z")).build()));
    assertEquals("in 23 hrs", DateTimeUtils.relativeDate(context, ksString, dateTime,
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
  public void testRelativeDate_withExplainFalse() {
    final Context context = context();
    final KSString ksString = ksString();
    final DateTime dateTime = DateTime.parse("2015-12-17T18:35:05Z");
    final DateTimeUtils.RelativeDateOptions.Builder builder = DateTimeUtils.RelativeDateOptions.builder().explain(false);

    assertEquals("4 hrs", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()));
    assertEquals("4 hrs", DateTimeUtils.relativeDate(context, ksString, dateTime,
      builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()));
  }
}
