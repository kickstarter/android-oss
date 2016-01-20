package com.kickstarter.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

public interface ApiClientType {
  @NonNull Observable<Config> config();

  @NonNull Observable<ActivityEnvelope> fetchActivities();

  @NonNull Observable<ActivityEnvelope> fetchActivities(final @Nullable Integer count);

  @NonNull Observable<ActivityEnvelope> fetchActivitiesWithPaginationPath(final @NonNull String paginationPath);

  @NonNull Observable<List<Category>> fetchCategories();

  @NonNull Observable<Category> fetchCategory(final @NonNull String param);

  @NonNull Observable<Category> fetchCategory(final @NonNull Category category);

  @NonNull Observable<User> fetchCurrentUser();

  @NonNull Observable<Location> fetchLocation(final @NonNull String param);

  @NonNull Observable<List<Notification>> fetchNotifications();

  @NonNull Observable<Project> fetchProject(final @NonNull String param);

  @NonNull Observable<Project> fetchProject(final @NonNull Project project);

  @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params);

  @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl);

  @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user);

  @NonNull Observable<CommentsEnvelope> fetchProjectComments(final @NonNull Project project);

  @NonNull Observable<CommentsEnvelope> fetchProjectComments(final @NonNull String paginationPath);

  @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam);

  @NonNull Observable<Update> fetchUpdate(final @NonNull Update update);

  @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken);

  @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code);

  @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password);

  @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password, final @NonNull String code);

  @NonNull Observable<Comment> postProjectComment(final @NonNull Project project, final @NonNull String body);

  @NonNull Observable<Empty> registerPushToken(final @NonNull String token);

  @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, boolean sendNewsletters);

  @NonNull Observable<User> resetPassword(final @NonNull String email);

  @NonNull Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email, final @NonNull String password,
    final @NonNull String passwordConfirmation, final boolean sendNewsletters);

  @NonNull Observable<Project> starProject(final @NonNull Project project);

  @NonNull Observable<Project> toggleProjectStar(final @NonNull Project project);

  @NonNull Observable<Notification> updateNotifications(final @NonNull Notification notification, final boolean checked);

  @NonNull Observable<User> updateUserSettings(final @NonNull User user);
}
