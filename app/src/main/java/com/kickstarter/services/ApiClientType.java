package com.kickstarter.services;

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

public interface ApiClientType {
  Observable<AccessTokenEnvelope> loginWithFacebook(String accessToken);

  Observable<AccessTokenEnvelope> loginWithFacebook(String fbAccessToken, String code);

  Observable<AccessTokenEnvelope> registerWithFacebook(String fbAccessToken, boolean sendNewsletters);

  Observable<ActivityEnvelope> fetchActivities();

  Observable<ActivityEnvelope> fetchActivities(String paginationPath);

  Observable<List<Notification>> fetchNotifications();

  Observable<List<Category>> fetchCategories();

  Observable<CommentsEnvelope> fetchProjectComments(Project project);

  Observable<CommentsEnvelope> fetchProjectComments(String paginationPath);

  Observable<DiscoverEnvelope> fetchProjects(DiscoveryParams params);

  Observable<DiscoverEnvelope> fetchProjects(String paginationUrl);

  Observable<Project> fetchProject(String param);

  Observable<Project> fetchProject(Project project);

  Observable<Backing> fetchProjectBacking(Project project, User user);

  Observable<Category> fetchCategory(long id);

  Observable<Category> fetchCategory(Category category);

  Observable<User> fetchCurrentUser();

  Observable<AccessTokenEnvelope> login(String email, String password);

  Observable<AccessTokenEnvelope> login(String email, String password, String code);

  Observable<Comment> postProjectComment(Project project, String body);

  Observable<Empty> registerPushToken(String token);

  Observable<User> resetPassword(String email);

  Observable<AccessTokenEnvelope> signup(String name, String email, String password, String passwordConfirmation,
    boolean sendNewsletters);

  Observable<Project> starProject(final Project project);

  Observable<Project> toggleProjectStar(Project project);

  Observable<Notification> updateProjectNotifications(Notification notification, boolean checked);

  Observable<User> updateUserSettings(User user);

  Observable<Config> config();
}
