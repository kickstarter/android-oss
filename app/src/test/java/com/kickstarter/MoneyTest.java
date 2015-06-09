package com.kickstarter;

import junit.framework.TestCase;

import static com.kickstarter.TestUtils.createMoney;

public class MoneyTest extends TestCase {
  public void testFormattedCurrencyForUSCurrencyAndUSUser() {
    assertEquals("$120", createMoney("US").formattedCurrency(120.0f, "US", "$", "USD"));
  }

  public void testFormattedCurrencyForUKCurrencyAndUSUser() {
    assertEquals("£120", createMoney("US").formattedCurrency(120.0f, "UK", "£", "GBP"));
  }

  public void testFormattedCurrencyForUKCurrencyAndUKUser() {
    assertEquals("£120", createMoney("UK").formattedCurrency(120.0f, "UK", "£", "GBP"));
  }

  public void testFormattedCurrencyForUSCurrencyAndUKUser() {
    assertEquals("$120 USD", createMoney("UK").formattedCurrency(120.0f, "US", "$", "USD"));
  }

  public void testFormattedCurrencyForCADCurrencyAndUSUser() {
    assertEquals("$120 CAD", createMoney("US").formattedCurrency(120.0f, "CA", "$", "CAD"));
  }

  public void testFormattedCurrencyForUSCurrencyAndCAUser() {
    assertEquals("$120 USD", createMoney("UK").formattedCurrency(120.0f, "US", "$", "USD"));
  }
}
