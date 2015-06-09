package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.Money;

import junit.framework.TestCase;

import static com.kickstarter.TestUtils.config;
import static com.kickstarter.TestUtils.mockConfigLoader;

public class MoneyTest extends TestCase {
  public void testFormattedCurrencyForUSCurrencyAndUSUser() {
    final Money money = new Money(mockConfigLoader(config("US")));
    assertEquals("$120", money.formattedCurrency(120.0f, "US", "$", "USD"));
  }

  public void testFormattedCurrencyForUKCurrencyAndUSUser() {
    final Money money = new Money(mockConfigLoader(config("US")));
    assertEquals("£120", money.formattedCurrency(120.0f, "UK", "£", "GBP"));
  }

  public void testFormattedCurrencyForUKCurrencyAndUKUser() {
    final Money money = new Money(mockConfigLoader(config("UK")));
    assertEquals("£120", money.formattedCurrency(120.0f, "UK", "£", "GBP"));
  }

  public void testFormattedCurrencyForUSCurrencyAndUKUser() {
    final Money money = new Money(mockConfigLoader(config("UK")));
    assertEquals("$120 USD", money.formattedCurrency(120.0f, "US", "$", "USD"));
  }

  public void testFormattedCurrencyForCADCurrencyAndUSUser() {
    final Money money = new Money(mockConfigLoader(config("US")));
    assertEquals("$120 CAD", money.formattedCurrency(120.0f, "CA", "$", "CAD"));
  }

  public void testFormattedCurrencyForUSCurrencyAndCAUser() {
    final Money money = new Money(mockConfigLoader(config("CA")));
    assertEquals("$120 USD", money.formattedCurrency(120.0f, "US", "$", "USD"));
  }
}
