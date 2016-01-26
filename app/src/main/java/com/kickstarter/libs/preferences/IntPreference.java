package com.kickstarter.libs.preferences;

// Source from:
// https://github.com/JakeWharton/u2020/tree/7363d27ee0356e24dcbd00dc6926d993ee56d6e2/src/main/java/com/jakewharton/u2020/data/prefs

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class IntPreference {
  private final SharedPreferences sharedPreferences;
  private final String key;
  private final int defaultValue;

  public IntPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key) {
    this(sharedPreferences, key, 0);
  }

  public IntPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key,
    final int defaultValue) {
    this.sharedPreferences = sharedPreferences;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  public int get() {
    return sharedPreferences.getInt(key, defaultValue);
  }

  public boolean isSet() {
    return sharedPreferences.contains(key);
  }

  public void set(final int value) {
    sharedPreferences.edit().putInt(key, value).apply();
  }

  public void delete() {
    sharedPreferences.edit().remove(key).apply();
  }
}
