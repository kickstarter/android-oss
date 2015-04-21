package com.kickstarter.services;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

import com.kickstarter.models.Project;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;

/*package*/ interface KickstarterService {
  @GET("/v1/discover")
  Observable<DiscoverEnvelope> fetchProjects();

  @GET("/v1/projects/{id}")
  public Observable<Project> fetchProject(@Path("id") Integer id);
}
