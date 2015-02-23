package com.kickstarter.services;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import com.kickstarter.models.Project;
import java.util.List;

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
}
