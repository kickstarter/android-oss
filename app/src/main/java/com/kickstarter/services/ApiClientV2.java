package com.kickstarter.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.rx.operators.ApiErrorOperatorV2;
import com.kickstarter.libs.rx.operators.OperatorsV2;
import com.kickstarter.libs.utils.extensions.AnyExtKt;
import com.kickstarter.libs.utils.extensions.BoolenExtKt;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.models.Reward;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apirequests.BackingBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.MessageBody;
import com.kickstarter.services.apirequests.PKCEBody;
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
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.services.apiresponses.OAuthTokenEnvelope;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;
import com.kickstarter.services.apiresponses.UpdatesEnvelope;
import com.kickstarter.ui.data.Mailbox;
import com.kickstarter.ui.data.MessageSubject;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public final class ApiClientV2 implements ApiClientTypeV2 {
  private final ApiServiceV2 service;
  private final Gson gson;

  public ApiClientV2(final @NonNull ApiServiceV2 service, final @NonNull Gson gson) {
    this.gson = gson;
    this.service = service;
  }

  @Override
  public @NonNull Observable<Config> config() {
    return this.service
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

    return this.service
      .activities(categories, count)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivitiesWithPaginationPath(final @NonNull String paginationPath) {
    return this.service
      .activities(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<List<Category>> fetchCategories() {
    return this.service
      .categories()
      .lift(apiErrorOperator())
      .map(CategoriesEnvelope::categories)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull String id) {
    return this.service
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
    return this.service
      .currentUser()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Location> fetchLocation(final @NonNull String param) {
    return this.service.location(param)
      .subscribeOn(Schedulers.io())
      .lift(apiErrorOperator());
  }

  @Override
  public @NonNull Observable<List<ProjectNotification>> fetchProjectNotifications() {
    return this.service
      .projectNotifications()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull String param) {
    return this.service
      .project(param)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull Project project) {
    return fetchProject(project.param()).startWith(project);
  }

  @Override
  public @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean isMember) {
    return this.service
      .projects(isMember ? 1 : 0)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
    return this.service
      .projects(params.queryParams())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl) {
    return this.service
      .projects(paginationUrl)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ProjectStatsEnvelope> fetchProjectStats(final @NonNull Project project) {
    return this.service
      .projectStats(project.param())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return this.service
      .projectBacking(project.param(), user.param())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
    return this.service
      .messagesForBacking(backing.projectId(), backing.backerId())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread) {
    return this.service
      .messagesForThread(messageThread.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull Long messageThreadId) {
    return this.service
      .messagesForThread(messageThreadId)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads(final @Nullable Project project,
    final @NonNull Mailbox mailbox) {

    final Observable<Response<MessageThreadsEnvelope>> apiResponse = project == null
      ? this.service.messageThreads(mailbox.getType())
      : this.service.messageThreads(project.id(), mailbox.getType());

    return apiResponse
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreadsWithPaginationPath(final @NonNull String paginationPath) {
    return this.service
      .paginatedMessageThreads(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ShippingRulesEnvelope> fetchShippingRules(final @NonNull Project project, final @NonNull Reward reward) {
    return this.service
      .shippingRules(project.id(), reward.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<SurveyResponse> fetchSurveyResponse(final long surveyResponseId) {
    return this.service
      .surveyResponse(surveyResponseId)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<List<SurveyResponse>> fetchUnansweredSurveys() {
    return this.service
      .unansweredSurveys()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam) {
    return this.service
      .update(projectParam, updateParam)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull Update update) {
    final String projectParam = AnyExtKt.numToString(update.projectId());
    final String updateParam = AnyExtKt.numToString(update.id());

    return fetchUpdate(projectParam, updateParam)
      .startWith(update);
  }

  @Override
  public @NonNull Observable<UpdatesEnvelope> fetchUpdates(final @NonNull Project project) {
    return this.service
            .updates(project.param())
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<UpdatesEnvelope> fetchUpdates(final @NonNull String paginationPath) {
    return this.service
            .paginatedUpdates(paginationPath)
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken) {
    return this.service
      .login(LoginWithFacebookBody.builder().accessToken(accessToken).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code) {
    return this.service
      .login(LoginWithFacebookBody.builder().accessToken(fbAccessToken).code(code).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<OAuthTokenEnvelope> loginWithCodes(final @NonNull String codeVerifier, final @NonNull String code, final @NonNull String clientId) {
    return this.service
            .login(PKCEBody.builder()
                    .code(code)
                    .codeVerifier(codeVerifier)
                    .clientId(clientId)
                    .build())
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
    return this.service
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
    return this.service
      .login(XauthBody.builder()
        .email(email)
        .password(password)
        .code(code)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThread> markAsRead(final @NonNull MessageThread messageThread) {
    return this.service
      .markAsRead(messageThread.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<JsonObject> registerPushToken(final @NonNull String token) {
    return this.service
      .registerPushToken(PushTokenBody.builder().token(token).pushServer("development").build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
    return this.service
      .login(RegisterWithFacebookBody.builder()
        .accessToken(fbAccessToken)
        .sendNewsletters(sendNewsletters)
        .newsletterOptIn(sendNewsletters)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<User> resetPassword(final @NonNull String email) {
    return this.service
      .resetPassword(ResetPasswordBody.builder().email(email).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Message> sendMessage(final @NonNull MessageSubject messageSubject, final @NonNull String body) {
    final MessageBody messageBody = MessageBody.builder().body(body).build();

    return messageSubject
      .value(
        backing -> this.service
          .sendMessageToBacking(backing.projectId(), backing.backerId(), messageBody),
        messageThread -> this.service
          .sendMessageToThread(messageThread.id(), messageBody),
        project -> this.service
          .sendMessageToProject(project.id(), messageBody)
      )
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email,
    final @NonNull String password, final @NonNull String passwordConfirmation,
    final boolean sendNewsletters) {
    return this.service
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
  public @NonNull Observable<Project> saveProject(final @NonNull Project project) {
    return this.service
      .starProject(project.param())
      .lift(apiErrorOperator())
      .map(StarEnvelope::project)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> toggleProjectSave(final @NonNull Project project) {
    return this.service
      .toggleProjectStar(project.param())
      .lift(apiErrorOperator())
      .map(StarEnvelope::project)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Backing> postBacking(final @NonNull Project project, final @NonNull Backing backing, final boolean checked) {
    return this.service
      .putProjectBacking(project.id(), backing.backerId(), BackingBody.builder()
        .backer(backing.backerId())
        .id(backing.id())
        .backerCompletedAt(checked ? 1 : 0)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ProjectNotification> updateProjectNotifications(final @NonNull ProjectNotification projectNotification, final boolean checked) {
    return this.service
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
    return this.service
      .updateUserSettings(
        SettingsBody.builder()
          .optedOutOfRecommendations(BoolenExtKt.isTrue(user.optedOutOfRecommendations()) ? 1 : 0)
          .notifyMobileOfBackings(BoolenExtKt.isTrue(user.notifyMobileOfBackings()))
          .notifyMobileOfComments(BoolenExtKt.isTrue(user.notifyMobileOfComments()))
          .notifyMobileOfCreatorEdu(BoolenExtKt.isTrue(user.notifyMobileOfCreatorEdu()))
          .notifyMobileOfFollower(BoolenExtKt.isTrue(user.notifyMobileOfFollower()))
          .notifyMobileOfFriendActivity(BoolenExtKt.isTrue(user.notifyMobileOfFriendActivity()))
          .notifyMobileOfMessages(BoolenExtKt.isTrue(user.notifyMobileOfMessages()))
          .notifyMobileOfPostLikes(BoolenExtKt.isTrue(user.notifyMobileOfPostLikes()))
          .notifyMobileOfUpdates(BoolenExtKt.isTrue(user.notifyMobileOfUpdates()))
          .notifyMobileOfMarketingUpdate(BoolenExtKt.isTrue(user.notifyMobileOfMarketingUpdate()))
          .notifyOfBackings(BoolenExtKt.isTrue(user.notifyOfBackings()))
          .notifyOfComments(BoolenExtKt.isTrue(user.notifyOfComments()))
          .notifyOfCommentReplies(BoolenExtKt.isTrue(user.notifyOfCommentReplies()))
          .notifyOfCreatorDigest(BoolenExtKt.isTrue(user.notifyOfCreatorDigest()))
          .notifyOfCreatorEdu(BoolenExtKt.isTrue(user.notifyOfCreatorEdu()))
          .notifyOfFollower(BoolenExtKt.isTrue(user.notifyOfFollower()))
          .notifyOfFriendActivity(BoolenExtKt.isTrue(user.notifyOfFriendActivity()))
          .notifyOfMessages(BoolenExtKt.isTrue(user.notifyOfMessages()))
          .notifyOfUpdates(BoolenExtKt.isTrue(user.notifyOfUpdates()))
          .alumniNewsletter(BoolenExtKt.isTrue(user.alumniNewsletter()) ? 1 : 0)
          .artsCultureNewsletter(BoolenExtKt.isTrue(user.artsCultureNewsletter()) ? 1 : 0)
          .filmNewsletter(BoolenExtKt.isTrue(user.filmNewsletter()) ? 1 : 0)
          .gamesNewsletter(BoolenExtKt.isTrue(user.gamesNewsletter()) ? 1 : 0)
          .happeningNewsletter(BoolenExtKt.isTrue(user.happeningNewsletter()) ? 1 : 0)
          .inventNewsletter(BoolenExtKt.isTrue(user.inventNewsletter()) ? 1 : 0)
          .musicNewsletter(BoolenExtKt.isTrue(user.musicNewsletter()) ? 1 : 0)
          .promoNewsletter(BoolenExtKt.isTrue(user.promoNewsletter()) ? 1 : 0)
          .publishingNewsletter(BoolenExtKt.isTrue(user.publishingNewsletter()) ? 1 : 0)
          .showPublicProfile(BoolenExtKt.isTrue(user.showPublicProfile()) ? 1 : 0)
          .social(BoolenExtKt.isTrue(user.social()) ? 1 : 0)
          .weeklyNewsletter(BoolenExtKt.isTrue(user.weeklyNewsletter()) ? 1 : 0)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @NonNull
  @Override
  public Observable<EmailVerificationEnvelope> verifyEmail(final @NonNull String token) {
    return this.service.verifyEmail(token)
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  /**
   * Utility to create a new {@link ApiErrorOperator}, saves us from littering references to gson throughout the client.
   */
  private @NonNull <T> ApiErrorOperatorV2<T> apiErrorOperator() {
    return OperatorsV2.apiError(this.gson);
  }
}
