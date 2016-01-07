package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Config;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Location;
import com.kickstarter.models.Notification;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import java.util.List;

import rx.Observable;

public interface ApiClientType {
  Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken);

  Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code);

  Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, boolean sendNewsletters);

  Observable<ActivityEnvelope> fetchActivities();

  Observable<ActivityEnvelope> fetchActivities(final @NonNull String paginationPath);

  Observable<List<Notification>> fetchNotifications();

  Observable<List<Category>> fetchCategories();

  Observable<CommentsEnvelope> fetchProjectComments(final @NonNull Project project);

  Observable<CommentsEnvelope> fetchProjectComments(final @NonNull String paginationPath);

  Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params);

  Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl);

  Observable<Project> fetchProject(final @NonNull String param);

  Observable<Project> fetchProject(final @NonNull Project project);

  Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user);

  Observable<Category> fetchCategory(final @NonNull String param);

  Observable<Category> fetchCategory(final @NonNull Category category);

  Observable<Location> fetchLocation(final @NonNull String param);

  Observable<User> fetchCurrentUser();

  Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password);

  Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password, final @NonNull String code);

  Observable<Comment> postProjectComment(final @NonNull Project project, final @NonNull String body);

  Observable<Empty> registerPushToken(final @NonNull String token);

  Observable<User> resetPassword(final @NonNull String email);

  Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email, final @NonNull String password,
    final @NonNull String passwordConfirmation, final boolean sendNewsletters);

  Observable<Project> starProject(final @NonNull Project project);

  Observable<Project> toggleProjectStar(final @NonNull Project project);

  Observable<Notification> updateNotifications(final @NonNull Notification notification, final boolean checked);

  Observable<User> updateUserSettings(final @NonNull User user);

  Observable<Config> config();
}
