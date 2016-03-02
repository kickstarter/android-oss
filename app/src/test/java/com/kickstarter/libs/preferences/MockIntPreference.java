package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class MockIntPreference implements IntPreferenceType {
  private final @NonNull BehaviorSubject<Integer> values = BehaviorSubject.create();

  public MockIntPreference() {
    values.onNext(null);
  }

  public MockIntPreference(final int value) {
    values.onNext(value);
  }

  @Override
  public int get() {
    return values.take(1).toBlocking().last();
  }

  @Override
  public boolean isSet() {
    return values.take(1).toBlocking().last() != null;
  }

  @Override
  public void set(final int value) {
    values.onNext(value);
  }

  @Override
  public void delete() {
    values.onNext(null);
  }

  public Observable<Integer> observable() {
    return values;
  }
}

