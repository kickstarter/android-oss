package com.kickstarter.services;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.DateTimeTypeConverter;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;

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

    Gson gson = new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

    RestAdapter restAdapter = new RestAdapter.Builder()
      .setConverter(new GsonConverter(gson))
      // TODO: extract this so we can switch HQ envs within the app. It's very useful.
      .setEndpoint("https://***REMOVED***")
      .setRequestInterceptor(requestInterceptor)
      .setLogLevel(RestAdapter.LogLevel.FULL)
      .build();

    service = restAdapter.create(KickstarterService.class);
  }

  public Observable<List<Project>> fetchProjects() {
    return service.fetchProjects()
      .retry(3)
      .map(envelope -> envelope.projects);
  }

  public Observable<Project> fetchProject(final Project project) {
    return Observable.just(project).mergeWith(service.fetchProject(project.id()));
  }
}
