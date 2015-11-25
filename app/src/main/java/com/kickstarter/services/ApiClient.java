package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.libs.rx.operators.ApiErrorOperator;
import com.kickstarter.libs.rx.operators.Operators;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apirequests.CommentBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.PushTokenBody;
import com.kickstarter.services.apirequests.RegisterWithFacebookBody;
import com.kickstarter.services.apirequests.ResetPasswordBody;
import com.kickstarter.services.apirequests.SignupBody;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CategoriesEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

public final class ApiClient {
  private final ApiService service;
  private final Gson gson;

  public ApiClient(@NonNull final ApiService service, @NonNull final Gson gson) {
    this.gson = gson;
    this.service = service;
  }

  public Observable<AccessTokenEnvelope> loginWithFacebook(@NonNull final String fbAccessToken) {
    return service.loginWithFacebook(LoginWithFacebookBody.builder().accessToken(fbAccessToken).build());
  }

  public Observable<AccessTokenEnvelope> loginWithFacebook(@NonNull final String fbAccessToken, @NonNull final String code) {
    return service.loginWithFacebook(
      LoginWithFacebookBody.builder()
        .accessToken(fbAccessToken)
        .code(code)
        .build()
    );
  }

  public Observable<AccessTokenEnvelope> registerWithFacebook(@NonNull final String fbAccessToken, final boolean sendNewsletters) {
    return service.registerWithFacebook(
      RegisterWithFacebookBody.builder()
        .accessToken(fbAccessToken)
        .sendNewsletters(sendNewsletters)
        .build()
    );
  }

  public Observable<ActivityEnvelope> fetchActivities(@NonNull final ActivityFeedParams params) {
    return service.fetchActivities(params.categoryParams(), params.paginationParams());
  }

  public Observable<List<Category>> fetchCategories() {
    return service.fetchCategories()
      .map(CategoriesEnvelope::categories)
      .subscribeOn(Schedulers.io());
  }

  public Observable<CommentsEnvelope> fetchProjectComments(@NonNull final CommentFeedParams params) {
    return service.fetchProjectComments(params.project().param(), params.paginationParams());
  }

  public Observable<DiscoverEnvelope> fetchProjects(@NonNull final DiscoveryParams params) {
    return service.fetchProjects(params.queryParams())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  public Observable<Project> fetchProject(@NonNull final String param) {
    return service.fetchProject(param);
  }

  public Observable<Project> fetchProject(@NonNull final Project project) {
    return fetchProject(project.param()).startWith(project);
  }

  public Observable<Backing> fetchProjectBacking(@NonNull final Project project, @NonNull final User user) {
    return service.fetchProjectBacking(project.param(), user.param());
  }

  public Observable<Category> fetchCategory(final long id) {
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

  public Observable<Comment> postProjectComment(@NonNull final Project project, @NonNull final String body) {
    return service.postProjectComment(project.param(), CommentBody.builder().body(body).build());
  }

  public @NonNull Observable<Empty> registerPushToken(@NonNull final String token) {
    return service.registerPushToken(PushTokenBody.builder().token(token).pushServer("development").build());
  }

  public @NonNull Observable<User> resetPassword(@NonNull final String email) {
    return service.resetPassword(ResetPasswordBody.builder().email(email).build());
  }

  public Observable<AccessTokenEnvelope> signup(@NonNull final String name, @NonNull final String email,
    @NonNull final String password, @NonNull final String passwordConfirmation,
    final boolean sendNewsletters) {
    return service.signup(SignupBody.builder()
      .name(name)
      .email(email)
      .password(password)
      .passwordConfirmation(passwordConfirmation)
      .sendNewsletters(sendNewsletters)
      .build());
  }

  public Observable<Project> starProject(@NonNull final Project project) {
    return service.starProject(project.param())
      .map(StarEnvelope::project);
  }

  public Observable<Project> toggleProjectStar(@NonNull final Project project) {
    return service.toggleProjectStar(project.param())
      .map(StarEnvelope::project);
  }

  /**
   * Utility to create a new {@link ApiErrorOperator}, saves us from littering references to gson throughout the client.
   */
  private @NonNull <T> ApiErrorOperator<T> apiErrorOperator() {
    return Operators.apiError(gson);
  }
}
