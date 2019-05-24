package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import java.math.RoundingMode;

import type.CurrencyCode;

public class KSCurrencyTest extends TestCase {
  public void testFormatCurrency_withUserInUS() {
    final KSCurrency currency = createKSCurrency("US");
    assertEquals("$100", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("$100 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("$101", currency.format(100.9f, ProjectFactory.project(), RoundingMode.UP));
    assertEquals("$101 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.UP));
  }

  public void testFormatCurrency_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");
    assertEquals("$100 USD", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("$100 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("$101 USD", currency.format(100.9f, ProjectFactory.project(), RoundingMode.UP));
    assertEquals("$101 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.UP));
  }

  public void testFormatCurrency_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");
    assertEquals("$100 USD", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("$100 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("$101 USD", currency.format(100.9f, ProjectFactory.project(), RoundingMode.UP));
    assertEquals("$101 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.UP));
  }

  public void testFormatCurrency_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");
    assertEquals("$100 USD", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("$100 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("$101 USD", currency.format(100.9f, ProjectFactory.project(), RoundingMode.UP));
    assertEquals("$101 CAD", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.UP));
  }

  public void testPreferUSD_withUserInUS() {
    final KSCurrency currency = createKSCurrency("US");

    final Project preferUSD_USProject = ProjectFactory.project().toBuilder().currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("$100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN));
    assertEquals("$101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.UP));

    final Project preferUSD_CAProject = ProjectFactory.caProject().toBuilder().currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("$75", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.DOWN));
    assertEquals("$76", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.UP));

    final Project preferUSD_UKProject = ProjectFactory.ukProject().toBuilder().currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("$150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN));
    assertEquals("$152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.UP));
  }

  public void testPreferUSD_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");

    final Project preferUSD_USProject = ProjectFactory.project().toBuilder().fxRate(1f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.UP));

    final Project preferUSD_CAProject = ProjectFactory.caProject().toBuilder().fxRate(.75f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.DOWN));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.UP));

    final Project preferUSD_UKProject = ProjectFactory.ukProject().toBuilder().fxRate(1.5f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN));
    assertEquals("US$ 152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.UP));
  }

  public void testPreferUSD_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");

    final Project preferUSD_USProject = ProjectFactory.project().toBuilder().fxRate(1f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.UP));

    final Project preferUSD_CAProject = ProjectFactory.caProject().toBuilder().fxRate(.75f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.DOWN));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.UP));

    final Project preferUSD_UKProject = ProjectFactory.ukProject().toBuilder().fxRate(1.5f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN));
    assertEquals("US$ 152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.UP));
  }

  public void testPreferUSD_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");

    final Project preferUSD_USProject = ProjectFactory.project().toBuilder().fxRate(1f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.UP));

    final Project preferUSD_CAProject = ProjectFactory.caProject().toBuilder().fxRate(.75f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.DOWN));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f,preferUSD_CAProject, RoundingMode.UP));

    final Project preferUSD_UKProject = ProjectFactory.ukProject().toBuilder().fxRate(1.5f).currentCurrency(CurrencyCode.USD.rawValue()).build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN));
    assertEquals("US$ 152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.UP));
  }

  public void testPreferCAD_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");

    final Project preferCAD_USProject = ProjectFactory.project().toBuilder().fxRate(1.5f).currentCurrency(CurrencyCode.CAD.rawValue()).build();
    assertEquals("CA$ 150", currency.formatWithUserPreference(100.9f, preferCAD_USProject, RoundingMode.DOWN));
    assertEquals("CA$ 152", currency.formatWithUserPreference(100.9f, preferCAD_USProject, RoundingMode.UP));

    final Project preferCAD_CAProject = ProjectFactory.caProject().toBuilder().fxRate(1f).currentCurrency(CurrencyCode.CAD.rawValue()).build();
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.9f,preferCAD_CAProject, RoundingMode.DOWN));
    assertEquals("CA$ 101", currency.formatWithUserPreference(100.9f,preferCAD_CAProject, RoundingMode.UP));

    final Project preferCAD_UKProject = ProjectFactory.ukProject().toBuilder().fxRate(.75f).currentCurrency(CurrencyCode.CAD.rawValue()).build();
    assertEquals("CA$ 75", currency.formatWithUserPreference(100.9f, preferCAD_UKProject, RoundingMode.DOWN));
    assertEquals("CA$ 76", currency.formatWithUserPreference(100.9f, preferCAD_UKProject, RoundingMode.UP));
  }

  public void testPreferGBP_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");

    final Project preferGBP_USProject = ProjectFactory.project().toBuilder().fxRate(.75f).currentCurrency(CurrencyCode.GBP.rawValue()).build();
    assertEquals("£75", currency.formatWithUserPreference(100.9f, preferGBP_USProject, RoundingMode.DOWN));
    assertEquals("£76", currency.formatWithUserPreference(100.9f, preferGBP_USProject, RoundingMode.UP));

    final Project preferGBP_CAProject = ProjectFactory.caProject().toBuilder().fxRate(1.5f).currentCurrency(CurrencyCode.GBP.rawValue()).build();
    assertEquals("£150", currency.formatWithUserPreference(100.9f,preferGBP_CAProject, RoundingMode.DOWN));
    assertEquals("£152", currency.formatWithUserPreference(100.9f,preferGBP_CAProject, RoundingMode.UP));

    final Project preferGBP_UKProject = ProjectFactory.ukProject().toBuilder().fxRate(1f).currentCurrency(CurrencyCode.GBP.rawValue()).build();
    assertEquals("£100", currency.formatWithUserPreference(100.9f, preferGBP_UKProject, RoundingMode.DOWN));
    assertEquals("£101", currency.formatWithUserPreference(100.9f, preferGBP_UKProject, RoundingMode.UP));
  }

  public void testFormatCurrency_withCurrencyCodeExcluded() {
    final KSCurrency currency = createKSCurrency("CA");
    assertEquals("$100", currency.format(100.0f, ProjectFactory.project(), true));
  }

  public void testFormatCurrency_withUserInUSAndUSDPreferred() {
    final KSCurrency currency = createKSCurrency("US");
    assertEquals("$150", currency.format(100.0f, ProjectFactory.ukProject(), false, true, RoundingMode.DOWN));
  }

  public void testFormatCurrency_withUserInUKAndUSDPreferred() {
    final KSCurrency currency = createKSCurrency("UK");
    assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject(), false, true, RoundingMode.DOWN));
  }

  private static KSCurrency createKSCurrency(final String countryCode) {
    final Config config = ConfigFactory.config().toBuilder()
      .countryCode(countryCode)
      .build();

    final CurrentConfigType currentConfig = new MockCurrentConfig();
    currentConfig.config(config);

    return new KSCurrency(currentConfig);
  }
}
