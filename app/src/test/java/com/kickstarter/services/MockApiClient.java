package com.kickstarter.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Config;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Location;
import com.kickstarter.models.Notification;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import java.util.List;

import rx.Observable;

public class MockApiClient implements ApiClientType {
  @Override
  public @NonNull Observable<Config> config() {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivities() {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivities(final @Nullable Integer count) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivitiesWithPaginationPath(final @NonNull String paginationPath) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<List<Category>> fetchCategories() {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<List<Notification>> fetchNotifications() {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull String param) {
    return Observable.just(
      ProjectFactory.project()
        .toBuilder()
        .slug(param)
        .build()
    );
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull Project project) {
    return Observable.just(project);
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<CommentsEnvelope> fetchProjectComments(final @NonNull Project project) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<CommentsEnvelope> fetchProjectComments(final @NonNull String paginationPath) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull Update update) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken,
    final @NonNull String code) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken,
    final boolean sendNewsletters) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull String param) {
    return Observable.just(CategoryFactory.musicCategory());
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull Category category) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<User> fetchCurrentUser() {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Location> fetchLocation(final @NonNull String param) {
    return Observable.just(LocationFactory.sydney());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password,
    final @NonNull String code) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Comment> postProjectComment(final @NonNull Project project, final @NonNull String body) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Empty> registerPushToken(final @NonNull String token) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<User> resetPassword(final @NonNull String email) {
    return Observable.just(UserFactory.user());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email,
    final @NonNull String password, final @NonNull String passwordConfirmation, final boolean sendNewsletters) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<Project> starProject(final @NonNull Project project) {
    return Observable.just(project.toBuilder().isStarred(true).build());
  }

  @Override
  public @NonNull Observable<Project> toggleProjectStar(final @NonNull Project project) {
    return Observable.just(project.toBuilder().isStarred(!project.isStarred()).build());
  }

  @Override
  public @NonNull Observable<Notification> updateNotifications(final @NonNull Notification notification, final boolean checked) {
    return Observable.empty();
  }

  @Override
  public @NonNull Observable<User> updateUserSettings(final @NonNull User user) {
    return Observable.empty();
  }
}
