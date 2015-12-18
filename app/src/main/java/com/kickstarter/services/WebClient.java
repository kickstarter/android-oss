package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import retrofit.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

public final class WebClient {
  private final WebService service;

  public WebClient(@NonNull final WebService service) {
    this.service = service;
  }

  public Observable<InternalBuildEnvelope> pingBeta() {
    return service.pingBeta()
      .filter(Response::isSuccess)
      .map(Response::body)
      .subscribeOn(Schedulers.io());
  }
}
