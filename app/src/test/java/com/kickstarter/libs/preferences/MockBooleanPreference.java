package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class MockBooleanPreference implements BooleanPreferenceType {
  private final @NonNull List<Boolean> values = new ArrayList<Boolean>();

  public MockBooleanPreference() {
    values.add(null);
  }

  public MockBooleanPreference(final boolean value) {
    values.add(value);
  }

  @Override
  public boolean get() {
    return values.get(values.size() -1);
  }

  @Override
  public boolean isSet() {
    return values.get(values.size() -1) != null;
  }

  @Override
  public void set(final boolean value) {
    values.add(value);
  }

  @Override
  public void delete() {
    values.add(null);
  }

  public @NonNull List<Boolean> values() {
    return values;
  }
}
