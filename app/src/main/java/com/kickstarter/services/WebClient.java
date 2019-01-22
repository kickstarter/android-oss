package com.kickstarter.services;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import androidx.annotation.NonNull;
import retrofit2.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

public final class WebClient implements WebClientType {
  private final WebService service;

  public WebClient(final @NonNull WebService service) {
    this.service = service;
  }

  public Observable<InternalBuildEnvelope> pingBeta() {
    return this.service.pingBeta()
      .filter(Response::isSuccessful)
      .map(Response::body)
      .subscribeOn(Schedulers.io());
  }
}
