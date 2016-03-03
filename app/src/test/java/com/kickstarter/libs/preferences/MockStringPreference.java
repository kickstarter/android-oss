package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class MockStringPreference implements StringPreferenceType {
  private final List<String> values = new ArrayList<String>();

  public MockStringPreference() {
    values.add(null);
  }

  public MockStringPreference(final String value) {
    values.add(value);
  }

  @Override
  public String get() {
    return values.get(values.size() - 1);
  }

  @Override
  public boolean isSet() {
    return values.get(values.size() - 1) != null;
  }

  @Override
  public void set(final String value) {
    values.add(value);
  }

  @Override
  public void delete() {
    values.add(null);
  }

  public @NonNull List<String> values() {
    return values;
  }
}

