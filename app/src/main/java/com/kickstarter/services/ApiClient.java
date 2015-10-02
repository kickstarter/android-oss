package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CategoriesEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;

import java.util.List;

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
  private final Gson gson;
  private final ApiService service;

  public ApiClient(final ApiEndpoint apiEndpoint,
    final Build build,
    final String clientId,
    final CurrentUser currentUser,
    final Gson gson) {
    this.apiEndpoint = apiEndpoint;
    this.build = build;
    this.clientId = clientId;
    this.currentUser = currentUser;
    this.gson = gson;

    service = apiService();
  }

  public Observable<ActivityEnvelope> fetchActivities(@NonNull final ActivityFeedParams params) {
    return service.fetchActivities(params.queryParams())
      .retry(3);
  }

  public Observable<List<Category>> fetchCategories() {
    return service.fetchCategories().map(CategoriesEnvelope::categories);
  }

  public Observable<CommentsEnvelope> fetchProjectComments(@NonNull final Project project) {
    return service.fetchProjectComments(project.param());
  }

  public Observable<DiscoverEnvelope> fetchProjects(@NonNull final DiscoveryParams params) {
    return service.fetchProjects(params.queryParams())
      .retry(3);
  }

  public Observable<Project> fetchProject(@NonNull final String param) {
    return service.fetchProject(param);
  }

  public Observable<Project> fetchProject(@NonNull final Project project) {
    return fetchProject(project.param()).startWith(project);
  }

  public Observable<Category> fetchCategory(@NonNull final long id) {
    return service.fetchCategory(id);
  }

  public Observable<Category> fetchCategory(@NonNull final Category category) {
    return fetchCategory(category.id());
  }

  public Observable<AccessTokenEnvelope> login(@NonNull final String email, @NonNull final String password) {
    return service.login(email, password);
  }

  public Observable<AccessTokenEnvelope> login(@NonNull final String email, @NonNull final String password,
    @NonNull final String code) {
    return service.login(email, password, code);
  }

  public Observable<Comment> postProjectComment(@NonNull final String body) {
    return service.postProjectComment(body);
  }

  public Observable<Project> starProject(@NonNull final Project project) {
    return service.starProject(project.param())
      .map(StarEnvelope::project);
  }

  public Observable<Project> toggleProjectStar(@NonNull final Project project) {
    return service.toggleProjectStar(project.param())
      .map(StarEnvelope::project);
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
