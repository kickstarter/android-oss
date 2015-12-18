package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import java.util.Locale;

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
}
