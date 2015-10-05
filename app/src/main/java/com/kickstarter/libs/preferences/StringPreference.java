package com.kickstarter.libs.preferences;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

// Source from:
// https://github.com/JakeWharton/u2020/tree/7363d27ee0356e24dcbd00dc6926d993ee56d6e2/src/main/java/com/jakewharton/u2020/data/prefs

public class StringPreference {
  private final SharedPreferences sharedPreferences;
  private final String key;
  private final String defaultValue;

  public StringPreference(@NonNull final SharedPreferences sharedPreferences, @NonNull final String key) {
    this(sharedPreferences, key, null);
  }

  public StringPreference(@NonNull final SharedPreferences sharedPreferences, @NonNull final String key,
    @Nullable final String defaultValue) {
    this.sharedPreferences = sharedPreferences;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  public String get() {
    return sharedPreferences.getString(key, defaultValue);
  }

  public boolean isSet() {
    return sharedPreferences.contains(key);
  }

  public void set(@NonNull final String value) {
    sharedPreferences.edit().putString(key, value).apply();
  }

  public void delete() {
    sharedPreferences.edit().remove(key).apply();
  }
}
