package com.kickstarter.services;

import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;

import retrofit.client.Response;
import retrofit.http.GET;
import rx.Observable;

/*package*/ interface KickstarterService {
  @GET("/mobile/beta/ping")
  Observable<InternalBuildEnvelope> pingBeta();
}
