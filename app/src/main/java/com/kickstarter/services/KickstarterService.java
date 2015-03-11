package com.kickstarter.services;

import retrofit.http.GET;
import rx.Observable;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;

/*package*/ interface KickstarterService {
  @GET("/v1/discover")
  Observable<DiscoverEnvelope> fetchProjects();
}
