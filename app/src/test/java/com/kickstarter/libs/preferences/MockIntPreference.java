package com.kickstarter.libs.preferences;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public final class MockIntPreference implements IntPreferenceType {
  private final List<Integer> values = new ArrayList<Integer>();

  public MockIntPreference() {
    this.values.add(null);
  }

  public MockIntPreference(final int value) {
    this.values.add(value);
  }

  @Override
  public int get() {
    return this.values.get(this.values.size() - 1);
  }

  @Override
  public boolean isSet() {
    return this.values.get(this.values.size() - 1) != null;
  }

  @Override
  public void set(final int value) {
    this.values.add(value);
  }

  @Override
  public void delete() {
    this.values.add(null);
  }

  public @NonNull List<Integer> values() {
    return this.values;
  }
}

