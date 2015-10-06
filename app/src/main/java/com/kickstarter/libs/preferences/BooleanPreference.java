package com.kickstarter.libs.preferences;

// Source from:
// https://github.com/JakeWharton/u2020/tree/7363d27ee0356e24dcbd00dc6926d993ee56d6e2/src/main/java/com/jakewharton/u2020/data/prefs

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class BooleanPreference {
  private final SharedPreferences sharedPreferences;
  private final String key;
  private final boolean defaultValue;

  public BooleanPreference(@NonNull final SharedPreferences sharedPreferences, @NonNull final String key) {
    this(sharedPreferences, key, false);
  }

  public BooleanPreference(@NonNull final SharedPreferences sharedPreferences, @NonNull final String key,
    final boolean defaultValue) {
    this.sharedPreferences = sharedPreferences;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  public boolean get() {
    return sharedPreferences.getBoolean(key, defaultValue);
  }

  public boolean isSet() {
    return sharedPreferences.contains(key);
  }

  public void set(final boolean value) {
    sharedPreferences.edit().putBoolean(key, value).apply();
  }

  public void delete() {
    sharedPreferences.edit().remove(key).apply();
  }
}
