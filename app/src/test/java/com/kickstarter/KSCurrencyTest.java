package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.models.Country;
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
    assertEquals("$100", currency.format(100.1f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject()));
    assertEquals("$100", currency.format(100.9f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject()));

    assertEquals("$100.10", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.10", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.10", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("$100.90", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.90", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.90", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("$100", currency.format(100f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  public void testFormatCurrency_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");
    assertEquals("US$ 100", currency.format(100.9f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject()));
    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject()));

    assertEquals("US$ 100.10", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.10", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.10", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 100.90", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.90", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.90", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 100", currency.format(100f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  public void testFormatCurrency_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");
    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject()));
    assertEquals("US$ 100", currency.format(100.9f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject()));

    assertEquals("US$ 100.10", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.10", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.10", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 100.90", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.90", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.90", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 100", currency.format(100f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  public void testFormatCurrency_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");
    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject()));
    assertEquals("US$ 100", currency.format(100.9f, ProjectFactory.project()));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject()));

    assertEquals("US$ 100.10", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.10", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.10", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 100.90", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100.90", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100.90", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 100", currency.format(100f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  public void testPreferUSD_withUserInUS() {
    final KSCurrency currency = createKSCurrency("US");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();

    assertEquals("$100", currency.formatWithUserPreference(100.1f, preferUSD_USProject));
    assertEquals("$100", currency.formatWithUserPreference(100.9f, preferUSD_USProject));

    assertEquals("$100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("$100.10", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 2));
    assertEquals("$101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("$100.90", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("$75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject));
    assertEquals("$75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject));

    assertEquals("$75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("$75.07", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));
    assertEquals("$76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("$75.68", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("$150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject));
    assertEquals("$150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject));

    assertEquals("$150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("$150.15", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
    assertEquals("$151", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("$151.35", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
  }

  public void testPreferUSD_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject));

    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 100.10", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 100.90", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject));

    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 75.07", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 75.68", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject));

    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 150.15", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 151", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 151.35", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
  }

  public void testPreferUSD_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject));

    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 100.10", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 100.90", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject));

    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 75.07", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 75.68", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject));

    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 150.15", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 151", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 151.35", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
  }

  public void testPreferUSD_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject));

    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 100.10", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 100.90", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject));

    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 75.07", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 75.68", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 2));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject));

    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 150.15", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
    assertEquals("US$ 151", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 151.35", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 2));
  }

  public void testPreferCAD_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");

    final Project preferCAD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.CAD.rawValue())
      .build();
    assertEquals("CA$ 150", currency.formatWithUserPreference(100.1f, preferCAD_USProject));
    assertEquals("CA$ 150", currency.formatWithUserPreference(100.9f, preferCAD_USProject));

    assertEquals("CA$ 150", currency.formatWithUserPreference(100.1f, preferCAD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 150.15", currency.formatWithUserPreference(100.1f, preferCAD_USProject, RoundingMode.HALF_UP, 2));
    assertEquals("CA$ 151", currency.formatWithUserPreference(100.9f, preferCAD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 151.35", currency.formatWithUserPreference(100.9f, preferCAD_USProject, RoundingMode.HALF_UP, 2));

    final Project preferCAD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.CAD.rawValue())
      .build();
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.1f, preferCAD_CAProject));
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.9f, preferCAD_CAProject));

    assertEquals("CA$ 100", currency.formatWithUserPreference(100.1f, preferCAD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 100.10", currency.formatWithUserPreference(100.1f, preferCAD_CAProject, RoundingMode.HALF_UP, 2));
    assertEquals("CA$ 101", currency.formatWithUserPreference(100.9f, preferCAD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 100.90", currency.formatWithUserPreference(100.9f, preferCAD_CAProject, RoundingMode.HALF_UP, 2));

    final Project preferCAD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.CAD.rawValue())
      .build();
    assertEquals("CA$ 75", currency.formatWithUserPreference(100.1f, preferCAD_UKProject));
    assertEquals("CA$ 75", currency.formatWithUserPreference(100.9f, preferCAD_UKProject));

    assertEquals("CA$ 75", currency.formatWithUserPreference(100.1f, preferCAD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 75.07", currency.formatWithUserPreference(100.1f, preferCAD_UKProject, RoundingMode.HALF_UP, 2));
    assertEquals("CA$ 76", currency.formatWithUserPreference(100.9f, preferCAD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 75.68", currency.formatWithUserPreference(100.9f, preferCAD_UKProject, RoundingMode.HALF_UP, 2));
  }

  public void testPreferGBP_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");

    final Project preferGBP_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.GBP.rawValue())
      .build();
    assertEquals("£75", currency.formatWithUserPreference(100.1f, preferGBP_USProject));
    assertEquals("£75", currency.formatWithUserPreference(100.9f, preferGBP_USProject));

    assertEquals("£75", currency.formatWithUserPreference(100.1f, preferGBP_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("£75.07", currency.formatWithUserPreference(100.1f, preferGBP_USProject, RoundingMode.HALF_UP, 2));
    assertEquals("£76", currency.formatWithUserPreference(100.9f, preferGBP_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("£75.68", currency.formatWithUserPreference(100.9f, preferGBP_USProject, RoundingMode.HALF_UP, 2));

    final Project preferGBP_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.GBP.rawValue())
      .build();
    assertEquals("£150", currency.formatWithUserPreference(100.1f, preferGBP_CAProject));
    assertEquals("£150", currency.formatWithUserPreference(100.9f, preferGBP_CAProject));

    assertEquals("£150", currency.formatWithUserPreference(100.1f, preferGBP_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("£150.15", currency.formatWithUserPreference(100.1f, preferGBP_CAProject, RoundingMode.HALF_UP, 2));
    assertEquals("£151", currency.formatWithUserPreference(100.9f, preferGBP_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("£151.35", currency.formatWithUserPreference(100.9f, preferGBP_CAProject, RoundingMode.HALF_UP, 2));

    final Project preferGBP_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.GBP.rawValue())
      .build();
    assertEquals("£100", currency.formatWithUserPreference(100.1f, preferGBP_UKProject));
    assertEquals("£100", currency.formatWithUserPreference(100.9f, preferGBP_UKProject));

    assertEquals("£100", currency.formatWithUserPreference(100.1f, preferGBP_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("£100.10", currency.formatWithUserPreference(100.1f, preferGBP_UKProject, RoundingMode.HALF_UP, 2));
    assertEquals("£101", currency.formatWithUserPreference(100.9f, preferGBP_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("£100.90", currency.formatWithUserPreference(100.9f, preferGBP_UKProject, RoundingMode.HALF_UP, 2));
  }

  public void testFormatCurrency_withCurrencyCodeExcluded() {
    final KSCurrency caCurrency = createKSCurrency("CA");
    assertEquals("US$ 100", caCurrency.format(100.0f, ProjectFactory.project(), true));
    assertEquals("US$ 100", caCurrency.format(100.0f, ProjectFactory.project(), false));
    assertEquals("CA$ 100", caCurrency.format(100.0f, ProjectFactory.caProject(), true));
    assertEquals("CA$ 100", caCurrency.format(100.0f, ProjectFactory.caProject(), false));

    final KSCurrency usCurrency = createKSCurrency("US");
    assertEquals("$100", usCurrency.format(100.0f, ProjectFactory.project(), true));
    assertEquals("US$ 100", usCurrency.format(100.0f, ProjectFactory.project(), false));
    assertEquals("CA$ 100", usCurrency.format(100.0f, ProjectFactory.caProject(), true));
    assertEquals("CA$ 100", usCurrency.format(100.0f, ProjectFactory.caProject(), false));
  }

  public void testCurrencyNeedsCode() {
    final KSCurrency usCurrency = createKSCurrency("US");
    assertFalse(usCurrency.currencyNeedsCode(Country.US, true));
    assertTrue(usCurrency.currencyNeedsCode(Country.US, false));

    final KSCurrency caCurrency = createKSCurrency("CA");
    assertTrue(caCurrency.currencyNeedsCode(Country.US, true));
    assertTrue(caCurrency.currencyNeedsCode(Country.US, false));

    final KSCurrency unlaunchedCurrency = createKSCurrency("XX");
    assertTrue(unlaunchedCurrency.currencyNeedsCode(Country.US, true));
    assertTrue(unlaunchedCurrency.currencyNeedsCode(Country.US, false));
  }


  public void testGetSymbolForCurrency() {
    final KSCurrency usCurrency = createKSCurrency("US");
    // US people looking at US currency just get the currency symbol.
    assertEquals("$", usCurrency.getCurrencySymbol(Country.US, true));
    assertEquals("\u00A0US$\u00A0", usCurrency.getCurrencySymbol(Country.US, false));
    // Singapore projects get a special currency prefix
    assertEquals("\u00A0S$\u00A0", usCurrency.getCurrencySymbol(Country.SG, false));
    // Kroner projects use the currency code prefix
    assertEquals("\u00A0CHF\u00A0", usCurrency.getCurrencySymbol(Country.CH, false));
    assertEquals("\u00A0DKK\u00A0", usCurrency.getCurrencySymbol(Country.DK, false));
    assertEquals("\u00A0NOK\u00A0", usCurrency.getCurrencySymbol(Country.NO, false));
    assertEquals("\u00A0SEK\u00A0", usCurrency.getCurrencySymbol(Country.SE, false));
    // Everything else
    assertEquals("\u00A0MX$\u00A0", usCurrency.getCurrencySymbol(Country.MX, false));

    final KSCurrency caCurrency = createKSCurrency("CA");
    assertEquals("\u00A0US$\u00A0", caCurrency.getCurrencySymbol(Country.US, true));
    assertEquals("\u00A0US$\u00A0", caCurrency.getCurrencySymbol(Country.US, false));

    final KSCurrency unlaunchedCurrency = createKSCurrency("XX");
    assertEquals("\u00A0US$\u00A0", unlaunchedCurrency.getCurrencySymbol(Country.US, true));
    assertEquals("\u00A0US$\u00A0", unlaunchedCurrency.getCurrencySymbol(Country.US, false));
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
