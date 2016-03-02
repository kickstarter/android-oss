package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class MockBooleanPreference implements BooleanPreferenceType {
  private final @NonNull BehaviorSubject<Boolean> values = BehaviorSubject.create();

  public MockBooleanPreference() {
    values.onNext(null);
  }

  public MockBooleanPreference(final boolean value) {
    values.onNext(value);
  }

  @Override
  public boolean get() {
    return values.take(1).toBlocking().last();
  }

  @Override
  public boolean isSet() {
    return values.take(1).toBlocking().last() != null;
  }

  @Override
  public void set(final boolean value) {
    values.onNext(value);
  }

  @Override
  public void delete() {
    values.onNext(null);
  }

  public Observable<Boolean> observable() {
    return values;
  }
}
