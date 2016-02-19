package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import rx.Observable;

public class MockWebClient implements WebClientType {
  @Override
  public @NonNull Observable<InternalBuildEnvelope> pingBeta() {
    return Observable.empty();
  }
}
