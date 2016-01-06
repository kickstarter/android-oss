package com.kickstarter.libs.utils;

import com.kickstarter.factories.LocationFactory;

import junit.framework.TestCase;

import java.util.Locale;

public final class I18nUtilsTest extends TestCase {
  public void testLanguage() {
    assertEquals("en", Locale.US.getLanguage());
    assertEquals("de", Locale.GERMANY.getLanguage());
  }

  public void testIsGermanLocale() {
    assertEquals(false, I18nUtils.isLocaleGermany(LocationFactory.unitedStates()));
    assertEquals(true, I18nUtils.isLocaleGermany(LocationFactory.germany()));
  }
}
