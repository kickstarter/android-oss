package com.kickstarter.services;

import com.kickstarter.models.Project;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import rx.Observable;

import com.kickstarter.models.Project;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;

import java.util.Map;

/*package*/ interface KickstarterService {
  @GET("/v1/discover")
  Observable<DiscoverEnvelope> fetchProjects(@QueryMap Map<String, String> params);

  @GET("/v1/projects/{id}")
  public Observable<Project> fetchProject(@Path("id") Integer id);
}
