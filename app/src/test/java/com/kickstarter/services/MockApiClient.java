package com.kickstarter.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.CommentFactory;
import com.kickstarter.factories.DiscoverEnvelopeFactory;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;

public class MockApiClient implements ApiClientType {
  private final PublishSubject<Pair<String, Map<String, Object>>> observable = PublishSubject.create();

  /**
   * Emits when endpoints on the client are called. The key in the pair is the underscore-separated
   * name of the method, and the value is a map of argument names/values.
   */
  public @NonNull Observable<Pair<String, Map<String, Object>>> observable() {
    return observable;
  }

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
    return Observable.just(
      DiscoverEnvelopeFactory.discoverEnvelope(Collections.singletonList(ProjectFactory.project()))
    );
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
    final Comment comment = CommentFactory.comment().toBuilder().body(body).build();
    observable.onNext(
      Pair.create("post_project_comment", new HashMap<String, Object>() {
        {
          put("comment", comment);
        }
      })
    );
    return Observable.just(comment);
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

    return Observable.just(
      AccessTokenEnvelope.builder()
        .user(UserFactory.user()
          .toBuilder()
          .name(name)
          .build()
        )
      .accessToken("deadbeef")
      .build()
    );
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
    observable.onNext(
      Pair.create("update_user_settings", new HashMap<String, Object>() {
        {
          put("user", user);
        }
      })
    );
    return Observable.just(user);
  }
}
