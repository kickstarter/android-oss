package com.kickstarter.services;

import retrofit.http.GET;
import rx.Observable;

/**
* Created by brandon on 2/23/15.
*/
/*package*/ interface KickstarterService {
  @GET("/v1/discover")
  Observable<ApiResponses.DiscoverEnvelope> fetchProjects();
}
