package com.kickstarter.libs.preferences;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class MockStringPreference implements StringPreferenceType {
  private final @NonNull BehaviorSubject<String> values = BehaviorSubject.create();

  public MockStringPreference() {
    values.onNext(null);
  }

  public MockStringPreference(final String value) {
    values.onNext(value);
  }

  @Override
  public String get() {
    return values.take(1).toBlocking().last();
  }

  @Override
  public boolean isSet() {
    return values.take(1).toBlocking().last() != null;
  }

  @Override
  public void set(final String value) {
    values.onNext(value);
  }

  @Override
  public void delete() {
    values.onNext(null);
  }

  public Observable<String> observable() {
    return values;
  }
}

