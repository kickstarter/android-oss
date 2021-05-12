package com.kickstarter.libs.utils;

import com.kickstarter.libs.NumberOptions;

import junit.framework.TestCase;

import java.math.RoundingMode;
import java.util.Locale;

public final class NumberUtilsTest extends TestCase {
  public void testFlooredPercentage() {
    assertEquals("50%", NumberUtils.flooredPercentage(50.0f));
    assertEquals("99%", NumberUtils.flooredPercentage(99.99f));
    assertEquals("0%", NumberUtils.flooredPercentage(0.01f));
    assertEquals("1,000%", NumberUtils.flooredPercentage(1000.0f));
  }

  public void testFlooredPercentage_withFrenchLocale() {
    assertEquals("50\u00A0%", NumberUtils.flooredPercentage(50.0f, Locale.FRENCH));
  }

  public void testFlooredPercentage_withGermanLocale() {
    assertEquals("1.000\u00A0%", NumberUtils.flooredPercentage(1000.0f, Locale.GERMAN));
  }

  public void testFormatNumber_int() {
    assertEquals("100", NumberUtils.format(100));
    assertEquals("1,000", NumberUtils.format(1000));
  }

  public void testFormatNumber_intWithGermanyLocale() {
    assertEquals("1.000", NumberUtils.format(1000, Locale.GERMANY));
  }

  public void testFormatNumber_float() {
    assertEquals("100", NumberUtils.format(100.0f));
    assertEquals("1,000", NumberUtils.format(1000.0f));
    assertEquals("1,001", NumberUtils.format(1000.6f));
  }

  public void testFormatNumber_floatRounding() {
    assertEquals("1", NumberUtils.format(1.1f));
    assertEquals("1", NumberUtils.format(1.5f));
    assertEquals("2", NumberUtils.format(2.5f));
    assertEquals("2", NumberUtils.format(1.51f));
    assertEquals("1", NumberUtils.format(1.9f, NumberOptions.builder().roundingMode(RoundingMode.DOWN).build()));
    assertEquals("2", NumberUtils.format(1.1f, NumberOptions.builder().roundingMode(RoundingMode.UP).build()));
  }

  public void testFormatNumber_floatWithPrecision() {
    assertEquals("100.12", NumberUtils.format(100.12f, NumberOptions.builder().precision(2).build()));
    assertEquals("100.2", NumberUtils.format(100.16f, NumberOptions.builder().precision(1).build()));
    assertEquals("100.00", NumberUtils.format(100.0f, NumberOptions.builder().precision(2).build()));
  }

  public void testFormatNumber_floatWithBucket() {
    assertEquals("100", NumberUtils.format(100.0f, NumberOptions.builder().bucketAbove(100.0f).build()));

    assertEquals("100", NumberUtils.format(100.0f, NumberOptions.builder().bucketAbove(1000.0f).build()));
    assertEquals("1K", NumberUtils.format(1000.0f, NumberOptions.builder().bucketAbove(1000.0f).build()));
    assertEquals("10K", NumberUtils.format(10000.0f, NumberOptions.builder().bucketAbove(1000.0f).build()));
    assertEquals("100K", NumberUtils.format(100_000.0f, NumberOptions.builder().bucketAbove(1000.0f).build()));
    assertEquals("1,000K", NumberUtils.format(1_000_000.0f, NumberOptions.builder().bucketAbove(1000.0f).build()));

    assertEquals("100", NumberUtils.format(100.0f, NumberOptions.builder().bucketAbove(10000.0f).build()));
    assertEquals("1,000", NumberUtils.format(1000.0f, NumberOptions.builder().bucketAbove(10000.0f).build()));
    assertEquals("10K", NumberUtils.format(10000.0f, NumberOptions.builder().bucketAbove(10000.0f).build()));
    assertEquals("100K", NumberUtils.format(100_000.0f, NumberOptions.builder().bucketAbove(10000.0f).build()));
    assertEquals("1,000K", NumberUtils.format(1_000_000.0f, NumberOptions.builder().bucketAbove(10000.0f).build()));

    assertEquals("100", NumberUtils.format(100.0f, NumberOptions.builder().bucketAbove(100_000.0f).build()));
    assertEquals("1,000", NumberUtils.format(1000.0f, NumberOptions.builder().bucketAbove(100_000.0f).build()));
    assertEquals("10,000", NumberUtils.format(10000.0f, NumberOptions.builder().bucketAbove(100_000.0f).build()));
    assertEquals("100K", NumberUtils.format(100_000.0f, NumberOptions.builder().bucketAbove(100_000.0f).build()));
    assertEquals("1,000K", NumberUtils.format(1_000_000.0f, NumberOptions.builder().bucketAbove(100_000.0f).build()));

    assertEquals("100", NumberUtils.format(100.0f, NumberOptions.builder().bucketAbove(1_000_000.0f).build()));
    assertEquals("1,000", NumberUtils.format(1000.0f, NumberOptions.builder().bucketAbove(1_000_000.0f).build()));
    assertEquals("10,000", NumberUtils.format(10000.0f, NumberOptions.builder().bucketAbove(1_000_000.0f).build()));
    assertEquals("100,000", NumberUtils.format(100_000.0f, NumberOptions.builder().bucketAbove(1_000_000.0f).build()));
    assertEquals("1M", NumberUtils.format(1_000_000.0f, NumberOptions.builder().bucketAbove(1_000_000.0f).build()));

    assertEquals("111", NumberUtils.format(111.0f, NumberOptions.builder().bucketAbove(1000.0f).bucketPrecision(1).build()));
    assertEquals("111.00", NumberUtils.format(111.0f, NumberOptions.builder().bucketAbove(111.0f).bucketPrecision(1).precision(2).build()));
    assertEquals("1.1K", NumberUtils.format(1111.0f, NumberOptions.builder().bucketAbove(1000.0f).bucketPrecision(1).build()));
    assertEquals("1.1K", NumberUtils.format(1111.0f, NumberOptions.builder().bucketAbove(1000.0f).bucketPrecision(1).precision(2).build()));
    assertEquals("11.1K", NumberUtils.format(11111.0f, NumberOptions.builder().bucketAbove(1000.0f).bucketPrecision(1).build()));
    assertEquals("111.1K", NumberUtils.format(111_111.0f, NumberOptions.builder().bucketAbove(1000.0f).bucketPrecision(1).build()));
    assertEquals("1,111.1K", NumberUtils.format(1_111_111.0f, NumberOptions.builder().bucketAbove(1000.0f).bucketPrecision(1).build()));
  }

  public void testFormatNumber_floatWithCurrency() {
    assertEquals("$100", NumberUtils.format(100.0f, NumberOptions.builder().currencySymbol("$").build()));
    assertEquals("€100", NumberUtils.format(100.0f, NumberOptions.builder().currencySymbol("€").build()));
    assertEquals("$100 CAD", NumberUtils.format(100.0f, NumberOptions.builder().currencySymbol("$").currencyCode("CAD").build()));
  }

  public void testFormatNumber_floatWithCurrencyAndGermanyLocale() {
    assertEquals("100\u00A0$", NumberUtils.format(100.0f, NumberOptions.builder().currencySymbol("$").build(), Locale.GERMANY));
    assertEquals("1.000", NumberUtils.format(1000.0f, NumberOptions.builder().build(), Locale.GERMANY));
    assertEquals("100,12", NumberUtils.format(100.12f, NumberOptions.builder().precision(2).build(), Locale.GERMANY));
  }

  public void testParse() {
    assertEquals(0.0, NumberUtils.parse(""));
    assertEquals(1.0, NumberUtils.parse("1"));
    assertEquals(1.5, NumberUtils.parse("1.5"));
    assertEquals(100.0, NumberUtils.parse("100"));
    assertEquals(100.5, NumberUtils.parse("100.50"));
    assertEquals(1000.0, NumberUtils.parse("1,000"));
    assertEquals(1000.5, NumberUtils.parse("1,000.50"));
    assertEquals(10000.0, NumberUtils.parse("10,000"));
    assertEquals(10000.5, NumberUtils.parse("10,000.50"));
  }

  public void testParse_withGermanLocale() {
    assertEquals(0.0, NumberUtils.parse("", Locale.GERMAN));
    assertEquals(1.0, NumberUtils.parse("1", Locale.GERMAN));
    assertEquals(1.5, NumberUtils.parse("1,5", Locale.GERMAN));
    assertEquals(100.0, NumberUtils.parse("100", Locale.GERMAN));
    assertEquals(100.5, NumberUtils.parse("100,50", Locale.GERMAN));
    assertEquals(1000.0, NumberUtils.parse("1.000", Locale.GERMAN));
    assertEquals(1000.5, NumberUtils.parse("1.000,50", Locale.GERMAN));
    assertEquals(10000.0, NumberUtils.parse("10.000", Locale.GERMAN));
    assertEquals(10000.5, NumberUtils.parse("10.000,50", Locale.GERMAN));
  }

  public void testPrecision() {
    assertEquals(0, NumberUtils.precision(1.0, RoundingMode.DOWN));
    assertEquals(0, NumberUtils.precision(1.5, RoundingMode.DOWN));
    assertEquals(0, NumberUtils.precision(1.0, RoundingMode.HALF_UP));
    assertEquals(2, NumberUtils.precision(1.5, RoundingMode.HALF_UP));
  }
}

