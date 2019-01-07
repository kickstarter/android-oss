package com.kickstarter.libs.utils;

import java.util.Locale;

import androidx.annotation.NonNull;

public final class I18nUtils {
  private I18nUtils() {}

  /**
   * Gets the language set on the device, or if none is found, just return "en" for english.
   *
   * This value can be changed while an app is running, so the value shouldn't be cached.
   */
  public static @NonNull String language() {
    final String language = Locale.getDefault().getLanguage();
    return language.isEmpty() ? "en" : language;
  }

  public static boolean isCountryGermany(final @NonNull String country) {
    return Locale.GERMANY.getCountry().equals(country);
  }

  public static boolean isCountryUS(final @NonNull String country) {
    return Locale.US.getCountry().equals(country);
  }
}
