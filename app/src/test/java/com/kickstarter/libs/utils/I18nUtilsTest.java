package com.kickstarter.libs.utils;

import com.kickstarter.factories.LocationFactory;

import junit.framework.TestCase;

import java.util.Locale;

public final class I18nUtilsTest extends TestCase {
  public void testLanguage() {
    assertEquals("en", Locale.US.getLanguage());
    assertEquals("de", Locale.GERMANY.getLanguage());
  }

  public void testIsCountryGermany() {
    assertFalse(I18nUtils.isCountryGermany(LocationFactory.unitedStates()));
    assertTrue(I18nUtils.isCountryGermany(LocationFactory.germany()));
  }

  public void testIsCountryUS() {
    assertTrue(I18nUtils.isCountryUS(LocationFactory.unitedStates()));
    assertFalse(I18nUtils.isCountryUS(LocationFactory.germany()));
  }
}
