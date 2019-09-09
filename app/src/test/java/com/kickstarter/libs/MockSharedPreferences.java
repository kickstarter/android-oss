package com.kickstarter.libs;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

public class MockSharedPreferences implements SharedPreferences {
  private final MapEditor editor;

  public MockSharedPreferences() {
    this.editor = new MapEditor();
  }

  @Override
  public Map<String, ?> getAll() {
    return this.editor.map;
  }

  @Nullable
  @Override
  public String getString(final String key, final String defValue) {
    return this.editor.map.containsKey(key)? (String) this.editor.map.get(key) : defValue;
  }

  @Nullable
  @Override
  public Set<String> getStringSet(final String key, final Set<String> defValues) {
    return this.editor.map.containsKey(key)? (Set<String>) this.editor.map.get(key) : defValues;
  }

  @Override
  public int getInt(final String key, final int defValue) {
    return this.editor.map.containsKey(key)? (int) this.editor.map.get(key) : defValue;
  }

  @Override
  public long getLong(final String key, final long defValue) {
    return this.editor.map.containsKey(key)? (long) this.editor.map.get(key) : defValue;
  }

  @Override
  public float getFloat(final String key, final float defValue) {
    return this.editor.map.containsKey(key)? (float) this.editor.map.get(key) : defValue;
  }

  @Override
  public boolean getBoolean(final String key, final boolean defValue) {
    return this.editor.map.containsKey(key)? (boolean) this.editor.map.get(key) : defValue;
  }

  @Override
  public boolean contains(final String key) {
    return this.editor.map.containsKey(key);
  }

  @Override
  public Editor edit() {
    return this.editor;
  }

  @Override
  public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
  }

  @Override
  public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
  }

  final class MapEditor implements Editor {

    final Map<String, Object> map = new HashMap<>();

    @Override
    public Editor putString(final String key, final @Nullable String value) {
      this.map.put(key, value);
      return this;
    }

    @Override
    public Editor putStringSet(final String key, final @Nullable Set<String> values) {
      this.map.put(key, values);
      return this;
    }

    @Override
    public Editor putInt(final String key, final int value) {
      this.map.put(key, value);
      return this;
    }

    @Override
    public Editor putLong(final String key, final long value) {
      this.map.put(key, value);
      return this;
    }

    @Override
    public Editor putFloat(final String key, final float value) {
      this.map.put(key, value);
      return this;
    }

    @Override
    public Editor putBoolean(final String key, final boolean value) {
      this.map.put(key, value);
      return this;
    }

    @Override
    public Editor remove(final String key) {
      this.map.remove(key);
      return this;
    }

    @Override
    public Editor clear() {
      this.map.clear();
      return this;
    }

    @Override
    public boolean commit() {
      return true;
    }

    @Override
    public void apply() {

    }
  }
}
