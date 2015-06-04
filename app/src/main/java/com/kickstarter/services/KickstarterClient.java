package com.kickstarter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.ActivityCategoryTypeConverter;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.ApiResponses.ActivityEnvelope;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;
import com.kickstarter.services.ApiResponses.ErrorEnvelope;

import org.joda.time.DateTime;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class KickstarterClient {
  private final KickstarterService service;
  private final CurrentUser currentUser;

  public KickstarterClient(final CurrentUser currentUser) {
    this.currentUser = currentUser;
    service = kickstarterService();
  }

  public Observable<ActivityEnvelope> fetchActivities(final ActivityFeedParams params) {
    return service.fetchActivities(params.queryParams()).retry(3);
  }

  public Observable<DiscoverEnvelope> fetchProjects(final DiscoveryParams params) {
    return service.fetchProjects(params.queryParams())
      .retry(3);
  }

  public Observable<Project> fetchProject(final Project project) {
    return Observable.just(project).mergeWith(service.fetchProject(project.id()));
  }

  public Observable<AccessTokenEnvelope> login(final String email, final String password) {
    return service.login(email, password);
  }

  public Observable<AccessTokenEnvelope> login(final String email, final String password, final String code) {
    return service.login(email, password, code);
  }

  private KickstarterService kickstarterService() {
    return restAdapter().create(KickstarterService.class);
  }

  private RestAdapter restAdapter() {
    return new RestAdapter.Builder()
      .setConverter(gsonConverter())
        // TODO: extract this so we can switch HQ envs within the app. It's very useful.
      .setEndpoint("https://***REMOVED***")
      .setErrorHandler(errorHandler())
      .setRequestInterceptor(requestInterceptor())
      .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
      .build();
  }

  private ErrorHandler errorHandler() {
    return cause -> {
      if (cause.getKind() == RetrofitError.Kind.HTTP) {
        final ErrorEnvelope envelope = (ErrorEnvelope) cause.getBodyAs(ErrorEnvelope.class);
        return new ApiError(cause, envelope);
      } else {
        // NETWORK or UNEXPECTED error.
        return cause;
      }
    };
  }

  private GsonConverter gsonConverter() {
    final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Activity.Category.class, new ActivityCategoryTypeConverter())
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

    return new GsonConverter(gson);
  }

  private RequestInterceptor requestInterceptor() {
    return request -> {
      request.addHeader("Accept", "application/json");
      request.addHeader("Kickstarter-Android-App", "1"); // TODO: Kickstarter app side
      // TODO: Look at Retrofit user agent
      // TODO: extract this so that it's easy to swap client_id for different HQ envs.
      request.addQueryParam("client_id", "***REMOVED***");
      if (currentUser.exists()) {
        request.addQueryParam("oauth_token", currentUser.getToken());
      }
    };
  }
}
