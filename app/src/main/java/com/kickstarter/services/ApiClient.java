package com.kickstarter.services;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.ActivityCategoryTypeConverter;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Build;
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

public class ApiClient {
  private final ApiEndpoint apiEndpoint;
  private final Build build;
  private final String clientId;
  private final CurrentUser currentUser;
  private final ApiService service;

  public ApiClient(final ApiEndpoint apiEndpoint, final Build build, final String clientId, final CurrentUser currentUser) {
    this.apiEndpoint = apiEndpoint;
    this.build = build;
    this.clientId = clientId;
    this.currentUser = currentUser;

    service = apiService();
  }

  public Observable<ActivityEnvelope> fetchActivities(final ActivityFeedParams params) {
    return service.fetchActivities(params.queryParams()).retry(3);
  }

  public Observable<DiscoverEnvelope> fetchProjects(final DiscoveryParams params) {
    return service.fetchProjects(params.queryParams())
      .retry(3);
  }

  public Observable<Project> fetchProject(final Project project) {
    return service.fetchProject(project.param()).startWith(project);
  }

  public Observable<AccessTokenEnvelope> login(final String email, final String password) {
    return service.login(email, password);
  }

  public Observable<AccessTokenEnvelope> login(final String email, final String password, final String code) {
    return service.login(email, password, code);
  }

  private ApiService apiService() {
    return restAdapter().create(ApiService.class);
  }

  private RestAdapter restAdapter() {
    return new RestAdapter.Builder()
      .setConverter(gsonConverter())
      .setEndpoint(apiEndpoint.url)
      .setErrorHandler(errorHandler())
      .setRequestInterceptor(requestInterceptor())
      .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.HEADERS_AND_ARGS : RestAdapter.LogLevel.NONE)
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
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Activity.Category.class, new ActivityCategoryTypeConverter())
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

    return new GsonConverter(gson);
  }

  private RequestInterceptor requestInterceptor() {
    return request -> {
      request.addHeader("Accept", "application/json");
      request.addHeader("Kickstarter-Android-App", build.versionCode().toString());
      request.addQueryParam("client_id", clientId());
      if (currentUser.exists()) {
        request.addQueryParam("oauth_token", currentUser.getAccessToken());
      }
    };
  }

  private String clientId() {
    return clientId;
  }
}
