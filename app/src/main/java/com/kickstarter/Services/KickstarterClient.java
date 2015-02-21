package com.kickstarter.Services;

import java.util.ArrayList;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import com.kickstarter.Models.Project;
import java.util.List;

/**
 * Created by brandon on 2/21/15.
 */
public class KickstarterClient {
  private final KickstarterService service;

  public KickstarterClient () {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override
      public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Accept", "application/json");
        // TODO: extract this so that it's easy to swap client_id for different HQ envs.
        request.addQueryParam("client_id", "***REMOVED***");
      }
    };

    RestAdapter restAdapter = new RestAdapter.Builder()
      // TODO: extract this so we can switch HQ envs within the app. It's very useful.
      .setEndpoint("https://***REMOVED***")
      .setRequestInterceptor(requestInterceptor)
      .setLogLevel(RestAdapter.LogLevel.FULL)
      .build();

    service = restAdapter.create(KickstarterService.class);
  }

  public Observable<List<Project>> fetchProjects () {
    return service.fetchProjects()
      .retry(3)
      .map(envelope -> envelope.projects);
  }

  private interface KickstarterService {
    @GET("/v1/discover")
    Observable<DiscoverEnvelope> fetchProjects ();
  }

  /**
   * A lightweight class whose scheme resembles that
   * of the API response for discovery endpoints.
   */
  private class DiscoverEnvelope {
    public List<Project> projects;
  }
}
