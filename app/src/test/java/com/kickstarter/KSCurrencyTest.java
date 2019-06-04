package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import org.junit.Test;

import java.math.RoundingMode;

import type.CurrencyCode;

public class KSCurrencyTest extends TestCase {

  public void testFormatCurrency_withUserInUS() {
    final KSCurrency currency = createKSCurrency("US");
    assertEquals("$100", currency.format(100.1f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.DOWN));
    assertEquals("$100", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("$100", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("$101", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 101", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  @Test
  public void testFormatCurrency_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");
    assertEquals("US$ 100", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));
    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 101", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 101", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  @Test
  public void testFormatCurrency_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");
    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.DOWN));
    assertEquals("US$ 100", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("US$ 100", currency.format(100.1f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 100", currency.format(100.1f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£100", currency.format(100.1f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
    assertEquals("US$ 101", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 101", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  @Test
  public void testFormatCurrency_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");
    assertEquals("US$ 100", currency.format(100.9f, ProjectFactory.project(), RoundingMode.DOWN));
    assertEquals("CA$ 100", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.DOWN));
    assertEquals("£100", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.DOWN));

    assertEquals("US$ 101", currency.format(100.9f, ProjectFactory.project(), RoundingMode.HALF_UP));
    assertEquals("CA$ 101", currency.format(100.9f, ProjectFactory.caProject(), RoundingMode.HALF_UP));
    assertEquals("£101", currency.format(100.9f, ProjectFactory.ukProject(), RoundingMode.HALF_UP));
  }

  public void testPreferUSD_withUserInUS() {
    final KSCurrency currency = createKSCurrency("US");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();

    assertEquals("$100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("$100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("$100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("$101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("$75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("$75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("$75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("$76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("$150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("$150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("$150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("$152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
  }

  public void testPreferUSD_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
  }

  public void testPreferUSD_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
  }

  public void testPreferUSD_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");

    final Project preferUSD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.1f, preferUSD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 101", currency.formatWithUserPreference(100.9f, preferUSD_USProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 75", currency.formatWithUserPreference(100.1f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 76", currency.formatWithUserPreference(100.9f, preferUSD_CAProject, RoundingMode.HALF_UP, 0));

    final Project preferUSD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.USD.rawValue())
      .build();
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("US$ 150", currency.formatWithUserPreference(100.1f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("US$ 152", currency.formatWithUserPreference(100.9f, preferUSD_UKProject, RoundingMode.HALF_UP, 0));
  }

  public void testPreferCAD_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");

    final Project preferCAD_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.CAD.rawValue())
      .build();
    assertEquals("CA$ 150", currency.formatWithUserPreference(100.1f, preferCAD_USProject, RoundingMode.DOWN, 0));
    assertEquals("CA$ 150", currency.formatWithUserPreference(100.9f, preferCAD_USProject, RoundingMode.DOWN, 0));
    assertEquals("CA$ 150", currency.formatWithUserPreference(100.1f, preferCAD_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 152", currency.formatWithUserPreference(100.9f, preferCAD_USProject, RoundingMode.HALF_UP, 0));

    final Project preferCAD_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.CAD.rawValue())
      .build();
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.1f, preferCAD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.9f, preferCAD_CAProject, RoundingMode.DOWN, 0));
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.1f, preferCAD_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 101", currency.formatWithUserPreference(100.9f, preferCAD_CAProject, RoundingMode.HALF_UP, 0));

    final Project preferCAD_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.CAD.rawValue())
      .build();
    assertEquals("CA$ 75", currency.formatWithUserPreference(100.1f, preferCAD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("CA$ 75", currency.formatWithUserPreference(100.9f, preferCAD_UKProject, RoundingMode.DOWN, 0));
    assertEquals("CA$ 75", currency.formatWithUserPreference(100.1f, preferCAD_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("CA$ 76", currency.formatWithUserPreference(100.9f, preferCAD_UKProject, RoundingMode.HALF_UP, 0));
  }

  public void testPreferGBP_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");

    final Project preferGBP_USProject = ProjectFactory.project()
      .toBuilder()
      .fxRate(.75f)
      .currentCurrency(CurrencyCode.GBP.rawValue())
      .build();
    assertEquals("£75", currency.formatWithUserPreference(100.1f, preferGBP_USProject, RoundingMode.DOWN, 0));
    assertEquals("£75", currency.formatWithUserPreference(100.9f, preferGBP_USProject, RoundingMode.DOWN, 0));
    assertEquals("£75", currency.formatWithUserPreference(100.1f, preferGBP_USProject, RoundingMode.HALF_UP, 0));
    assertEquals("£76", currency.formatWithUserPreference(100.9f, preferGBP_USProject, RoundingMode.HALF_UP, 0));

    final Project preferGBP_CAProject = ProjectFactory.caProject()
      .toBuilder()
      .fxRate(1.5f)
      .currentCurrency(CurrencyCode.GBP.rawValue())
      .build();
    assertEquals("£150", currency.formatWithUserPreference(100.1f, preferGBP_CAProject, RoundingMode.DOWN, 0));
    assertEquals("£150", currency.formatWithUserPreference(100.9f, preferGBP_CAProject, RoundingMode.DOWN, 0));
    assertEquals("£150", currency.formatWithUserPreference(100.1f, preferGBP_CAProject, RoundingMode.HALF_UP, 0));
    assertEquals("£152", currency.formatWithUserPreference(100.9f, preferGBP_CAProject, RoundingMode.HALF_UP, 0));

    final Project preferGBP_UKProject = ProjectFactory.ukProject()
      .toBuilder()
      .fxRate(1f)
      .currentCurrency(CurrencyCode.GBP.rawValue())
      .build();
    assertEquals("£100", currency.formatWithUserPreference(100.1f, preferGBP_UKProject, RoundingMode.DOWN, 0));
    assertEquals("£100", currency.formatWithUserPreference(100.9f, preferGBP_UKProject, RoundingMode.DOWN, 0));
    assertEquals("£100", currency.formatWithUserPreference(100.1f, preferGBP_UKProject, RoundingMode.HALF_UP, 0));
    assertEquals("£101", currency.formatWithUserPreference(100.9f, preferGBP_UKProject, RoundingMode.HALF_UP, 0));
  }

  @Test
  public void testFormatWithProjectCurrency() {
    final KSCurrency currency = createKSCurrency("US");
    final Project project = ProjectFactory.project();
    assertEquals("$100.00", currency.formatWithProjectCurrency(100, project, RoundingMode.UP, 2).toString());
  }

  @Test
  public void testFormatWithUserPreference() {
    final KSCurrency currency = createKSCurrency("US");
    final Project project = ProjectFactory.project();
    assertEquals("$101", currency.formatWithUserPreference(100.1f, project, RoundingMode.UP, 0));
    assertEquals("$100", currency.formatWithUserPreference(100.9f, project, RoundingMode.DOWN, 0));
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

  private static KSCurrency createKSCurrency(final String countryCode) {
    final Config config = ConfigFactory.config().toBuilder()
      .countryCode(countryCode)
      .build();

    final CurrentConfigType currentConfig = new MockCurrentConfig();
    currentConfig.config(config);

    return new KSCurrency(currentConfig);
  }
}
