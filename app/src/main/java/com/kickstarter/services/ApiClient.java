package com.kickstarter.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.rx.operators.ApiErrorOperator;
import com.kickstarter.libs.rx.operators.Operators;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apirequests.CommentBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.ProjectNotificationBody;
import com.kickstarter.services.apirequests.PushTokenBody;
import com.kickstarter.services.apirequests.RegisterWithFacebookBody;
import com.kickstarter.services.apirequests.ResetPasswordBody;
import com.kickstarter.services.apirequests.SettingsBody;
import com.kickstarter.services.apirequests.SignupBody;
import com.kickstarter.services.apirequests.XauthBody;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CategoriesEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;

import java.util.Arrays;
import java.util.List;

import retrofit2.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class ApiClient implements ApiClientType {
  private final ApiService service;
  private final Gson gson;

  public ApiClient(final @NonNull ApiService service, final @NonNull Gson gson) {
    this.gson = gson;
    this.service = service;
  }

  @Override
  public @NonNull Observable<Config> config() {
    return service
      .config()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivities() {
    return fetchActivities(null);
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivities(final @Nullable Integer count) {
    final List<String> categories = Arrays.asList(
      Activity.CATEGORY_BACKING,
      Activity.CATEGORY_CANCELLATION,
      Activity.CATEGORY_FAILURE,
      Activity.CATEGORY_LAUNCH,
      Activity.CATEGORY_SUCCESS,
      Activity.CATEGORY_UPDATE,
      Activity.CATEGORY_FOLLOW
    );

    return service
      .activities(categories, count)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivitiesWithPaginationPath(final @NonNull String paginationPath) {
    return service
      .activities(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<List<Category>> fetchCategories() {
    return service
      .categories()
      .lift(apiErrorOperator())
      .map(CategoriesEnvelope::categories)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull String id) {
    return service
      .category(id)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull Category category) {
    return fetchCategory(String.valueOf(category.id()));
  }

  @Override
  public @NonNull Observable<User> fetchCurrentUser() {
    return service
      .currentUser()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Location> fetchLocation(final @NonNull String param) {
    return service.location(param)
      .subscribeOn(Schedulers.io())
      .lift(apiErrorOperator());
  }

  @Override
  public @NonNull Observable<List<ProjectNotification>> fetchProjectNotifications() {
    return service
      .projectNotifications()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull String param) {
    return service
      .project(param)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull Project project) {
    return fetchProject(project.param()).startWith(project);
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
    return service
      .projects(params.queryParams())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl) {
    return service
      .projects(paginationUrl)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return service
      .projectBacking(project.param(), user.param())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull Project project) {
    return service
      .projectComments(project.param())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull Update update) {
    return service
      .updateComments(update.projectId(), update.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull String paginationPath) {
    return service
      .paginatedProjectComments(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads() {
    return fetchMessageThreads(null);
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads(final @Nullable Project project) {
    final Observable<Response<MessageThreadsEnvelope>> apiResponse = project == null
      ? service.messageThreads()
      : service.messageThreads(project.id());

    return apiResponse
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreadsWithPaginationPath(final @NonNull String paginationPath) {
    return service
      .paginatedMessageThreads(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam) {
    return service
      .update(projectParam, updateParam)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull Update update) {
    final String projectParam = ObjectUtils.toString(update.projectId());
    final String updateParam = ObjectUtils.toString(update.id());

    return fetchUpdate(projectParam, updateParam)
      .startWith(update);
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken) {
    return service
      .login(LoginWithFacebookBody.builder().accessToken(accessToken).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code) {
    return service
      .login(LoginWithFacebookBody.builder().accessToken(fbAccessToken).code(code).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
    return service
      .login(RegisterWithFacebookBody.builder()
        .accessToken(fbAccessToken)
        .sendNewsletters(sendNewsletters)
        .newsletterOptIn(sendNewsletters)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
    return service
      .login(XauthBody.builder()
        .email(email)
        .password(password)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password,
    final @NonNull String code) {
    return service
      .login(XauthBody.builder()
        .email(email)
        .password(password)
        .code(code)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Comment> postComment(final @NonNull Project project, final @NonNull String body) {
    return service
      .postProjectComment(project.param(), CommentBody.builder().body(body).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Comment> postComment(final @NonNull Update update, final @NonNull String body) {
    return service
      .postUpdateComment(update.projectId(), update.id(), CommentBody.builder().body(body).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Empty> registerPushToken(final @NonNull String token) {
    return service
      .registerPushToken(PushTokenBody.builder().token(token).pushServer("development").build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<User> resetPassword(final @NonNull String email) {
    return service
      .resetPassword(ResetPasswordBody.builder().email(email).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email,
    final @NonNull String password, final @NonNull String passwordConfirmation,
    final boolean sendNewsletters) {
    return service
      .signup(
        SignupBody.builder()
          .name(name)
          .email(email)
          .password(password)
          .passwordConfirmation(passwordConfirmation)
          .sendNewsletters(sendNewsletters)
          .newsletterOptIn(sendNewsletters)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> starProject(final @NonNull Project project) {
    return service
      .starProject(project.param())
      .lift(apiErrorOperator())
      .map(StarEnvelope::project)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> toggleProjectStar(final @NonNull Project project) {
    return service
      .toggleProjectStar(project.param())
      .lift(apiErrorOperator())
      .map(StarEnvelope::project)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ProjectNotification> updateProjectNotifications(final @NonNull ProjectNotification projectNotification, final boolean checked) {
    return service
      .updateProjectNotifications(projectNotification.id(),
        ProjectNotificationBody.builder()
          .email(checked)
          .mobile(checked)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<User> updateUserSettings(final @NonNull User user) {
    return service
      .updateUserSettings(
        SettingsBody.builder()
          .notifyMobileOfFollower(isTrue(user.notifyMobileOfFollower()))
          .notifyMobileOfFriendActivity(isTrue(user.notifyMobileOfFriendActivity()))
          .notifyMobileOfUpdates(isTrue(user.notifyMobileOfUpdates()))
          .notifyOfFollower(isTrue(user.notifyOfFollower()))
          .notifyOfFriendActivity(isTrue(user.notifyOfFriendActivity()))
          .notifyOfUpdates(isTrue(user.notifyOfUpdates()))
          .gamesNewsletter(isTrue(user.gamesNewsletter()) ? 1 : 0)
          .happeningNewsletter(isTrue(user.happeningNewsletter()) ? 1 : 0)
          .promoNewsletter(isTrue(user.promoNewsletter()) ? 1 : 0)
          .weeklyNewsletter(isTrue(user.weeklyNewsletter()) ? 1 : 0)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  /**
   * Utility to create a new {@link ApiErrorOperator}, saves us from littering references to gson throughout the client.
   */
  private @NonNull <T> ApiErrorOperator<T> apiErrorOperator() {
    return Operators.apiError(gson);
  }
}
