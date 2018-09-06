package com.kickstarter.libs.utils;

import com.kickstarter.mock.factories.LocationFactory;

import junit.framework.TestCase;

import java.util.Locale;

public final class I18nUtilsTest extends TestCase {
  public void testLanguage() {
    assertEquals("en", Locale.US.getLanguage());
    assertEquals("de", Locale.GERMANY.getLanguage());
  }

  public void testIsCountryGermany() {
    assertFalse(I18nUtils.isCountryGermany(LocationFactory.unitedStates().country()));
    assertTrue(I18nUtils.isCountryGermany(LocationFactory.germany().country()));
  }

  public void testIsCountryUS() {
    assertTrue(I18nUtils.isCountryUS(LocationFactory.unitedStates().country()));
    assertFalse(I18nUtils.isCountryUS(LocationFactory.germany().country()));
  }
}
