package com.kickstarter.libs.preferences;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public final class MockBooleanPreference implements BooleanPreferenceType {
  private final @NonNull List<Boolean> values = new ArrayList<Boolean>();

  public MockBooleanPreference() {
    this.values.add(null);
  }

  public MockBooleanPreference(final boolean value) {
    this.values.add(value);
  }

  @Override
  public boolean get() {
    return this.values.get(this.values.size() -1);
  }

  @Override
  public boolean isSet() {
    return this.values.get(this.values.size() -1) != null;
  }

  @Override
  public void set(final boolean value) {
    this.values.add(value);
  }

  @Override
  public void delete() {
    this.values.add(null);
  }

  public @NonNull List<Boolean> values() {
    return this.values;
  }
}
