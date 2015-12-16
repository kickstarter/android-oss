package com.kickstarter;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Money;

import junit.framework.TestCase;

import static com.kickstarter.TestUtils.createMoney;

public class MoneyTest extends TestCase {
  public void testFormatCurrency_withUserInUS() {
    final Money money = createMoney("US");
    assertEquals("$100", money.formatCurrency(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", money.formatCurrency(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", money.formatCurrency(100.0f, ProjectFactory.ukProject()));
  }

  public void testFormatCurrency_withUserInCA() {
    final Money money = createMoney("CA");
    assertEquals("$100 USD", money.formatCurrency(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", money.formatCurrency(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", money.formatCurrency(100.0f, ProjectFactory.ukProject()));
  }

  public void testFormatCurrency_withUserInUK() {
    final Money money = createMoney("UK");
    assertEquals("$100 USD", money.formatCurrency(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", money.formatCurrency(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", money.formatCurrency(100.0f, ProjectFactory.ukProject()));
  }

  public void testFormatCurrency_withUserInUnlaunchedCountry() {
    final Money money = createMoney("XX");
    assertEquals("$100 USD", money.formatCurrency(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", money.formatCurrency(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", money.formatCurrency(100.0f, ProjectFactory.ukProject()));
  }

  public void testFormatCurrency_withCurrencyCodeExcluded() {
    final Money money = createMoney("CA");
    assertEquals("$100", money.formatCurrency(100.0f, ProjectFactory.project(), true));
  }

  public void testFormatCurrency_withUserInUSAndUSDPreferred() {
    final Money money = createMoney("US");
    assertEquals("$150", money.formatCurrency(100.0f, ProjectFactory.ukProject(), false, true));
  }

  public void testFormatCurrency_withUserInUKAndUSDPreferred() {
    final Money money = createMoney("UK");
    assertEquals("£100", money.formatCurrency(100.0f, ProjectFactory.ukProject(), false, true));
  }
}
