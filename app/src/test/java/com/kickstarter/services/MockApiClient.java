package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Config;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Notification;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import java.util.List;

import rx.Observable;

public class MockApiClient implements ApiClientType {
  @Override
  public Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken) {
    return Observable.empty();
  }
  @Override
  public Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken,
    final @NonNull String code) {
    return Observable.empty();
  }
  @Override
  public Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken,
    final boolean sendNewsletters) {
    return Observable.empty();
  }
  @Override
  public Observable<ActivityEnvelope> fetchActivities() {
    return Observable.empty();
  }
  @Override
  public Observable<ActivityEnvelope> fetchActivities(final @NonNull String paginationPath) {
    return Observable.empty();
  }
  @Override
  public Observable<List<Notification>> fetchNotifications() {
    return Observable.empty();
  }
  @Override
  public Observable<List<Category>> fetchCategories() {
    return Observable.empty();
  }
  @Override
  public Observable<CommentsEnvelope> fetchProjectComments(final @NonNull Project project) {
    return Observable.empty();
  }
  @Override
  public Observable<CommentsEnvelope> fetchProjectComments(final @NonNull String paginationPath) {
    return Observable.empty();
  }
  @Override
  public Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
    return Observable.empty();
  }
  @Override
  public Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl) {
    return Observable.empty();
  }
  @Override
  public Observable<Project> fetchProject(final @NonNull String param) {
    return Observable.just(ProjectFactory.project());
  }
  @Override
  public Observable<Project> fetchProject(final @NonNull Project project) {
    return Observable.empty();
  }
  @Override
  public Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return Observable.empty();
  }
  @Override
  public Observable<Category> fetchCategory(long id) {
    return Observable.empty();
  }
  @Override
  public Observable<Category> fetchCategory(final @NonNull Category category) {
    return Observable.empty();
  }
  @Override
  public Observable<User> fetchCurrentUser() {
    return Observable.empty();
  }
  @Override
  public Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
    return Observable.empty();
  }
  @Override
  public Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password,
    final @NonNull String code) {
    return Observable.empty();
  }
  @Override
  public Observable<Comment> postProjectComment(final @NonNull Project project, final @NonNull String body) {
    return Observable.empty();
  }
  @Override
  public Observable<Empty> registerPushToken(final @NonNull String token) {
    return Observable.empty();
  }
  @Override
  public Observable<User> resetPassword(final @NonNull String email) {
    return Observable.empty();
  }
  @Override
  public Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email,
    final @NonNull String password, final @NonNull String passwordConfirmation, final boolean sendNewsletters) {
    return Observable.empty();
  }
  @Override
  public Observable<Project> starProject(final @NonNull Project project) {
    return Observable.empty();
  }
  @Override
  public Observable<Project> toggleProjectStar(final @NonNull Project project) {
    return Observable.empty();
  }
  @Override
  public Observable<Notification> updateProjectNotifications(final @NonNull Notification notification, final boolean checked) {
    return Observable.empty();
  }
  @Override
  public Observable<User> updateUserSettings(final @NonNull User user) {
    return Observable.empty();
  }
  @Override
  public Observable<Config> config() {
    return Observable.empty();
  }
}
