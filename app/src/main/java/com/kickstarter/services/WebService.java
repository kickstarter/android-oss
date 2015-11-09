package com.kickstarter.services;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import retrofit.http.GET;
import rx.Observable;

/*package*/ interface WebService {
  @GET("/mobile/beta/ping")
  Observable<InternalBuildEnvelope> pingBeta();
}
