package com.kickstarter;

import com.kickstarter.libs.ConfigLoader;
import com.kickstarter.libs.Money;

import junit.framework.TestCase;

import static com.kickstarter.TestUtils.mockConfigLoader;


public class MoneyTest extends TestCase {
  private ConfigLoader configLoader;

  public void setUp() {
    configLoader = mockConfigLoader();
  }

  public void testFormattedCurrencyForUSProjectAndUSUser() {
    Money money = new Money(configLoader);
    assertEquals("$120", money.formattedCurrency(120.0f, "US", "$", "USD"));
  }

  public void testFormattedCurrencyForUKProjectAndUSUser() {
    Money money = new Money(configLoader);
    assertEquals("£120", money.formattedCurrency(120.0f, "UK", "£", "GBP"));
  }

/*  public void testFormattedCurrencyForCADProjectAndUSUser() {
    Money money = new Money(configLoader);
    assertEquals("$120 CAD", money.formattedCurrency(120.0f, "CA", "$", "CAD"));
  }*/
}
