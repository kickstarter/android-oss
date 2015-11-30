package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import rx.Observable;

public final class WebClient {
  private final WebService service;

  public WebClient(@NonNull final WebService service) {
    this.service = service;
  }

  public Observable<InternalBuildEnvelope> pingBeta() {
    return service.pingBeta();
  }
}
