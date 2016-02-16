package com.kickstarter.services;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import retrofit2.Response;
import retrofit2.http.GET;
import rx.Observable;

public interface WebService {
  @GET("/mobile/beta/ping")
  Observable<Response<InternalBuildEnvelope>> pingBeta();
}
