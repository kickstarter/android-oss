package com.kickstarter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.models.DiscoveryParams;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;

import org.joda.time.DateTime;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class KickstarterClient {
  private final KickstarterService service;

  public KickstarterClient() {
    RequestInterceptor requestInterceptor = request -> {
      request.addHeader("Accept", "application/json");
      // TODO: extract this so that it's easy to swap client_id for different HQ envs.
      request.addQueryParam("client_id", "***REMOVED***");
    };

    Gson gson = new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

    RestAdapter restAdapter = new RestAdapter.Builder()
      .setConverter(new GsonConverter(gson))
        // TODO: extract this so we can switch HQ envs within the app. It's very useful.
      .setEndpoint("https://***REMOVED***")
      .setRequestInterceptor(requestInterceptor)
      .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
      .build();

    service = restAdapter.create(KickstarterService.class);
  }

  public Observable<DiscoverEnvelope> fetchProjects(final DiscoveryParams params) {
    return service.fetchProjects(params.queryParams())
      .retry(3);
  }

  public Observable<Project> fetchProject(final Project project) {
    return Observable.just(project).mergeWith(service.fetchProject(project.id()));
  }

  public Observable<AccessTokenEnvelope> login(final String email, final String password) {
    return login(email, password, "");
  }

  public Observable<AccessTokenEnvelope> login(final String email, final String password, final String code) {
    return service.login(email, password, code);
  }
}
