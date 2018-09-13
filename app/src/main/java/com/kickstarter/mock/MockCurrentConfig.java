package com.kickstarter.mock;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class MockCurrentConfig implements CurrentConfigType {

  private final BehaviorSubject<Config> config = BehaviorSubject.create();

  @Override
  public @NonNull Observable<Config> observable() {
    return this.config;
  }

  @Override
  public @NonNull Config getConfig() {
    return this.config.getValue();
  }

  @Override
  public void config(final @NonNull Config config) {
    this.config.onNext(config);
  }
}
