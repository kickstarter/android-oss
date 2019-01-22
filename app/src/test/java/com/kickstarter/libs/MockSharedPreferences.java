package com.kickstarter.libs;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

// TODO: Finish this implementation by storing all the shared prefs data into arrays/maps.
public class MockSharedPreferences implements SharedPreferences {
  public MockSharedPreferences() {}

  @Override
  public Map<String, ?> getAll() {
    return null;
  }

  @Nullable
  @Override
  public String getString(final String key, final String defValue) {
    return null;
  }

  @Nullable
  @Override
  public Set<String> getStringSet(final String key, final Set<String> defValues) {
    return null;
  }

  @Override
  public int getInt(final String key, final int defValue) {
    return 0;
  }

  @Override
  public long getLong(final String key, final long defValue) {
    return 0;
  }

  @Override
  public float getFloat(final String key, final float defValue) {
    return 0;
  }

  @Override
  public boolean getBoolean(final String key, final boolean defValue) {
    return false;
  }

  @Override
  public boolean contains(final String key) {
    return false;
  }

  @Override
  public Editor edit() {
    return null;
  }

  @Override
  public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
  }

  @Override
  public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
  }
}
