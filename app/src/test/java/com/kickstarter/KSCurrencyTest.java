package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.models.Project;

import org.junit.Test;

import java.math.RoundingMode;

public class KSCurrencyTest extends KSRobolectricTestCase {
  @Test
  public void testFormatCurrency_withUserInUS() {
    final KSCurrency currency = createKSCurrency("US");
    assertEquals("$100", currency.format(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", currency.format(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject()));

    assertEquals("$100", currency.formatWithUserPreference(100.0f, ProjectFactory.project(), RoundingMode.DOWN, 0));
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.caProject(), RoundingMode.DOWN, 0));
    assertEquals("£100", currency.formatWithUserPreference(100.0f, ProjectFactory.ukProject(), RoundingMode.DOWN, 0));
  }

  @Test
  public void testFormatCurrency_withUserInCA() {
    final KSCurrency currency = createKSCurrency("CA");
    assertEquals("$100 USD", currency.format(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", currency.format(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject()));

    assertEquals("US$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.project(), RoundingMode.DOWN, 0));
    assertEquals("£100", currency.formatWithUserPreference(100.0f, ProjectFactory.ukProject(), RoundingMode.DOWN, 0));
    assertEquals("CA$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.caProject(), RoundingMode.DOWN, 0));
  }

  @Test
  public void testFormatCurrency_withUserInUK() {
    final KSCurrency currency = createKSCurrency("UK");
    assertEquals("$100 USD", currency.format(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", currency.format(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject()));

    assertEquals("CA$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.caProject(), RoundingMode.DOWN, 0));
    assertEquals("£100", currency.formatWithUserPreference(100.0f, ProjectFactory.ukProject(), RoundingMode.DOWN, 0));
  }

  @Test
  public void testFormatCurrency_withUserInUnlaunchedCountry() {
    final KSCurrency currency = createKSCurrency("XX");
    assertEquals("$100 USD", currency.format(100.0f, ProjectFactory.project()));
    assertEquals("$100 CAD", currency.format(100.0f, ProjectFactory.caProject()));
    assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject()));

    assertEquals("US$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.project(), RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.caProject(), RoundingMode.DOWN, 0));
    assertEquals("US$ 100", currency.formatWithUserPreference(100.0f, ProjectFactory.ukProject(), RoundingMode.DOWN, 0));
  }

  @Test
  public void testFormatCurrency_withCurrencyCodeExcluded() {
    final KSCurrency currency = createKSCurrency("CA");
    assertEquals("$100", currency.format(100.0f, ProjectFactory.project(), true));
  }

  @Test
  public void testFormatCurrency_withUserInUSAndUSDPreferred() {
    final KSCurrency currency = createKSCurrency("US");
    assertEquals("$150", currency.format(100.0f, ProjectFactory.ukProject(), false, true, RoundingMode.DOWN));
  }

  @Test
  public void testFormatCurrency_withUserInUKAndUSDPreferred() {
    final KSCurrency currency = createKSCurrency("UK");
    assertEquals("£100", currency.format(100.0f, ProjectFactory.ukProject(), false, true, RoundingMode.DOWN));
  }

  @Test
  public void testFormatCurrency_roundsDown() {
    final KSCurrency currency = createKSCurrency("US");
    final Project project = ProjectFactory.project();
    assertEquals("$100", currency.format(100.4f, project));
    assertEquals("$100", currency.format(100.5f, project));
    assertEquals("$101", currency.format(101.5f, project));
    assertEquals("$100", currency.format(100.9f, project));
  }

  @Test
  public void testFormatWithProjectCurrency() {
    final KSCurrency currency = createKSCurrency("US");
    final Project project = ProjectFactory.project();
    assertEquals("$100.00", currency.formatWithProjectCurrency(100, project, RoundingMode.UP, 2).toString());
  }

  public void testFormatWithUserPreference() {
    final KSCurrency currency = createKSCurrency("US");
    final Project project = ProjectFactory.project();
    assertEquals("$101", currency.formatWithUserPreference(100.1f, project, RoundingMode.UP));
    assertEquals("$100", currency.formatWithUserPreference(100.9f, project, RoundingMode.DOWN));
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
