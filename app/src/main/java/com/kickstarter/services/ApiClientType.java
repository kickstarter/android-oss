package com.kickstarter.services;

import com.google.gson.JsonObject;
import com.kickstarter.libs.Config;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Location;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.models.Reward;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope;
import com.kickstarter.services.apiresponses.UpdatesEnvelope;
import com.kickstarter.ui.data.Mailbox;
import com.kickstarter.ui.data.MessageSubject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

  @NonNull Observable<List<ProjectNotification>> fetchProjectNotifications();

  @NonNull Observable<Project> fetchProject(final @NonNull String param);

  @NonNull Observable<Project> fetchProject(final @NonNull Project project);

  @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean isMember);

  @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params);

  @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl);

  @NonNull Observable<ProjectStatsEnvelope> fetchProjectStats(final @NonNull Project project);

  @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user);

  @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull Project project);

  @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull String paginationPath);

  @NonNull Observable<CommentsEnvelope> fetchComments(final @NonNull Update update);

  @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing);

  @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread);

  @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull Long messageThreadId);

  @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads(final @Nullable Project project, final @NonNull Mailbox mailbox);

  @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreadsWithPaginationPath(final @NonNull String paginationPath);

  @NonNull Observable<ShippingRulesEnvelope> fetchShippingRules(final @NonNull Project project, final @NonNull Reward reward);

  @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam);

  @NonNull Observable<Update> fetchUpdate(final @NonNull Update update);

  @NonNull Observable<UpdatesEnvelope> fetchUpdates(final @NonNull Project project);

  @NonNull Observable<UpdatesEnvelope> fetchUpdates(final @NonNull String paginationPath);

  @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken);

  @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code);

  @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password);

  @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password, final @NonNull String code);

  @NonNull Observable<MessageThread> markAsRead(final @NonNull MessageThread messageThread);

  @NonNull Observable<Backing> postBacking(final @NonNull Project project, final @NonNull Backing backing, final boolean checked);

  @NonNull Observable<Comment> postComment(final @NonNull Project project, final @NonNull String body);

  @NonNull Observable<Comment> postComment(final @NonNull Update update, final @NonNull String body);

  @NonNull Observable<JsonObject> registerPushToken(final @NonNull String token);

  @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, boolean sendNewsletters);

  @NonNull Observable<User> resetPassword(final @NonNull String email);

  @NonNull Observable<Message> sendMessage(final @NonNull MessageSubject messageSubject, final @NonNull String body);

  @NonNull Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email, final @NonNull String password,
    final @NonNull String passwordConfirmation, final boolean sendNewsletters);

  @NonNull Observable<Project> saveProject(final @NonNull Project project);

  @NonNull Observable<SurveyResponse> fetchSurveyResponse(final long surveyResponseId);

  @NonNull Observable<Project> toggleProjectSave(final @NonNull Project project);

  @NonNull Observable<List<SurveyResponse>> fetchUnansweredSurveys();

  @NonNull Observable<ProjectNotification> updateProjectNotifications(final @NonNull ProjectNotification projectNotification, final boolean checked);

  @NonNull Observable<User> updateUserSettings(final @NonNull User user);
}
