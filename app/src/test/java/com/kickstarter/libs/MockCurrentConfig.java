package com.kickstarter.libs;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class MockCurrentConfig implements CurrentConfigType {

  private final BehaviorSubject<Config> config = BehaviorSubject.create();

  @Override
  public @NonNull Observable<Config> observable() {
    return config;
  }

  @Override
  public @NonNull Config getConfig() {
    return config.getValue();
  }

  @Override
  public void config(final @NonNull Config config) {
    this.config.onNext(config);
  }
}
