package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class MockIntPreference implements IntPreferenceType {
  private final List<Integer> values = new ArrayList<Integer>();

  public MockIntPreference() {
    values.add(null);
  }

  public MockIntPreference(final int value) {
    values.add(value);
  }

  @Override
  public int get() {
    return values.get(values.size() - 1);
  }

  @Override
  public boolean isSet() {
    return values.get(values.size() - 1) != null;
  }

  @Override
  public void set(final int value) {
    values.add(value);
  }

  @Override
  public void delete() {
    values.add(null);
  }

  public @NonNull List<Integer> values() {
    return values;
  }
}

