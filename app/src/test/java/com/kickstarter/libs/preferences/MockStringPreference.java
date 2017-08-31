package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class MockStringPreference implements StringPreferenceType {
  private final List<String> values = new ArrayList<>();

  public MockStringPreference() {
    this.values.add(null);
  }

  public MockStringPreference(final String value) {
    this.values.add(value);
  }

  @Override
  public String get() {
    return this.values.get(this.values.size() - 1);
  }

  @Override
  public boolean isSet() {
    return this.values.get(this.values.size() - 1) != null;
  }

  @Override
  public void set(final String value) {
    this.values.add(value);
  }

  @Override
  public void delete() {
    this.values.add(null);
  }

  public @NonNull List<String> values() {
    return this.values;
  }
}

