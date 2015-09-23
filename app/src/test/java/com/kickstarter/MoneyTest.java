package com.kickstarter;

import com.kickstarter.libs.CurrencyOptions;

import junit.framework.TestCase;

import static com.kickstarter.TestUtils.createMoney;

public class MoneyTest extends TestCase {
  public void testFormattedCurrencyForFlooredValue() {
    assertEquals("120", createMoney("US").formattedNumber(120.01f));
    assertEquals("120", createMoney("US").formattedNumber(120.50f));
    assertEquals("120", createMoney("US").formattedNumber(120.99f));
  }

  public void testFormattedCurrencyForUSCurrencyAndUSUser() {
    assertEquals("$120", createMoney("US").formattedCurrency(120.0f, USCurrencyOptions()));
  }

  public void testFormattedCurrencyForUKCurrencyAndUSUser() {
    assertEquals("£120", createMoney("US").formattedCurrency(120.0f, UKCurrencyOptions()));
  }

  public void testFormattedCurrencyForUKCurrencyAndUKUser() {
    assertEquals("£120", createMoney("UK").formattedCurrency(120.0f, UKCurrencyOptions()));
  }

  public void testFormattedCurrencyForUSCurrencyAndUKUser() {
    assertEquals("$120 USD", createMoney("UK").formattedCurrency(120.0f, USCurrencyOptions()));
  }

  public void testFormattedCurrencyForCADCurrencyAndUSUser() {
    assertEquals("$120 CAD", createMoney("US").formattedCurrency(120.0f, CACurrencyOptions()));
  }

  public void testFormattedCurrencyForUSCurrencyAndCAUser() {
    assertEquals("$120 USD", createMoney("UK").formattedCurrency(120.0f, USCurrencyOptions()));
  }

  public void testFormattedCurrencyForCADCurrencyAndUSUserWithExcludedCurrencyCode() {
    assertEquals("$120", createMoney("US").formattedCurrency(120.0f, CACurrencyOptions(), true));
  }

  private CurrencyOptions CACurrencyOptions() {
    return new CurrencyOptions("CA", "$", "CAD");
  }

  private CurrencyOptions UKCurrencyOptions() {
    return new CurrencyOptions("UK", "£", "GBP");
  }

  private CurrencyOptions USCurrencyOptions() {
    return new CurrencyOptions("US", "$", "USD");
  }
}
