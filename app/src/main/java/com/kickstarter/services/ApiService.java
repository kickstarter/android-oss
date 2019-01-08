package com.kickstarter.services;

import com.kickstarter.libs.Config;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Location;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apirequests.BackingBody;
import com.kickstarter.services.apirequests.CommentBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.MessageBody;
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
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

public interface ApiService {
  @GET("/v1/activities")
  Observable<Response<ActivityEnvelope>> activities(@NonNull @Query("categories[]") List<String> categories,
    @Nullable @Query("count") Integer count);

  @GET
  Observable<Response<ActivityEnvelope>> activities(@Url @NonNull String paginationUrl);

  @GET("/v1/categories")
  Observable<Response<CategoriesEnvelope>> categories();

  @GET("/v1/categories/{param}")
  Observable<Response<Category>> category(@Path("param") String param);

  @GET("/v1/app/android/config")
  Observable<Response<Config>> config();

  @GET("/v1/users/self")
  Observable<Response<User>> currentUser();

  @GET("/v1/locations/{param}")
  Observable<Response<Location>> location(@Path("param") String param);

  @POST("/xauth/access_token")
  Observable<Response<AccessTokenEnvelope>> login(@Body XauthBody body);

  @PUT("/v1/facebook/access_token?intent=login")
  Observable<Response<AccessTokenEnvelope>> login(@Body LoginWithFacebookBody body);

  @PUT("/v1/facebook/access_token?intent=register")
  Observable<Response<AccessTokenEnvelope>> login(@Body RegisterWithFacebookBody body);

  @PUT("/v1/message_threads/{message_thread_id}/read")
  Observable<Response<MessageThread>> markAsRead(@Path("message_thread_id") long messageThreadId);

  @GET("/v1/projects/{project_id}/backers/{backer_id}/messages")
  Observable<Response<MessageThreadEnvelope>> messagesForBacking(
    @Path("project_id") long projectId, @Path("backer_id") long backerId
  );

  @GET("/v1/message_threads/{message_thread_id}/messages")
  Observable<Response<MessageThreadEnvelope>> messagesForThread(@Path("message_thread_id") long messageThreadId);

  @GET("/v1/message_threads/{mailbox}")
  Observable<Response<MessageThreadsEnvelope>> messageThreads(@Path("mailbox") String mailbox);

  @GET("/v1/projects/{project_id}/message_threads/{mailbox}")
  Observable<Response<MessageThreadsEnvelope>> messageThreads(
    @Path("project_id") long projectId,
    @Path("mailbox") String mailbox
  );

  @GET
  Observable<Response<CommentsEnvelope>> paginatedProjectComments(@Url String paginationPath);

  @GET
  Observable<Response<MessageThreadsEnvelope>> paginatedMessageThreads(@Url String paginationPath);

  @POST("/v1/projects/{param}/comments/")
  Observable<Response<Comment>> postProjectComment(@Path("param") String param, @Body CommentBody body);

  @POST("/v1/projects/{project_id}/updates/{update_id}/comments")
  Observable<Response<Comment>> postUpdateComment(@Path("project_id") long projectId, @Path("update_id") long updateId,
    @Body CommentBody body);

  @GET("/v1/projects/{project_param}/backers/{user_param}")
  Observable<Response<Backing>> projectBacking(
    @Path("project_param") String projectParam,
    @Path("user_param") String userParam
  );

  @PUT("/v1/projects/{project_param}/backers/{user_param}")
  Observable<Response<Backing>> putProjectBacking(
    @Path("project_param") long projectParam,
    @Path("user_param") long userParam,
    @Body BackingBody backingBody
  );

  @GET("/v1/projects/{param}")
  Observable<Response<Project>> project(@Path("param") String param);

  @GET("/v1/projects/{project_param}/comments")
  Observable<Response<CommentsEnvelope>> projectComments(@Path("project_param") String projectParam);

  @GET("/v1/users/self/notifications")
  Observable<Response<List<ProjectNotification>>> projectNotifications();

  @GET("/v1/users/self/projects")
  Observable<Response<ProjectsEnvelope>> projects(@Query("member") int isMember);

  @GET("/v1/discover")
  Observable<Response<DiscoverEnvelope>> projects(@QueryMap Map<String, String> params);

  @GET
  Observable<Response<DiscoverEnvelope>> projects(@Url String paginationUrl);

  @GET("/v1/projects/{project_param}/stats")
  Observable<Response<ProjectStatsEnvelope>> projectStats(@Path("project_param") String projectParam);

  @POST("/v1/users/self/push_tokens")
  Observable<Response<Empty>> registerPushToken(@Body PushTokenBody body);

  @POST("/v1/users/reset")
  Observable<Response<User>> resetPassword(@Body ResetPasswordBody body);

  @POST("/v1/message_threads/{message_thread_id}/messages")
  Observable<Response<Message>> sendMessageToThread(@Path("message_thread_id") long messageThreadId, @Body MessageBody body);

  @POST("/v1/projects/{project_id}/backers/{backer_id}/messages")
  Observable<Response<Message>> sendMessageToBacking(
    @Path("project_id") long projectId, @Path("backer_id") long backerId, @Body MessageBody body
  );

  @POST("/v1/projects/{project_id}/messages")
  Observable<Response<Message>> sendMessageToProject(@Path("project_id") long projectId, @Body MessageBody body);

  @POST("/v1/users")
  Observable<Response<AccessTokenEnvelope>> signup(@Body SignupBody body);

  @PUT("/v1/projects/{param}/star")
  Observable<Response<StarEnvelope>> starProject(@Path("param") String param);

  @GET("/v1/users/self/surveys/{survey_response_id}")
  Observable<Response<SurveyResponse>> surveyResponse(@Path("survey_response_id") long surveyResponseId);

  @POST("/v1/projects/{param}/star/toggle")
  Observable<Response<StarEnvelope>> toggleProjectStar(@Path("param") String param);

  @GET("/v1/users/self/surveys/unanswered")
  Observable<Response<List<SurveyResponse>>> unansweredSurveys();

  @GET("/v1/projects/{project_param}/updates/{update_param}")
  Observable<Response<Update>> update(@Path("project_param") String projectParam, @Path("update_param") String updateParam);

  @GET("/v1/projects/{project_id}/updates/{update_id}/comments")
  Observable<Response<CommentsEnvelope>> updateComments(@Path("project_id") long projectId, @Path("update_id") long updateId);

  @PUT("/v1/users/self/notifications/{id}")
  Observable<Response<ProjectNotification>> updateProjectNotifications(@Path("id") long projectNotificationId,
    @Body ProjectNotificationBody projectNotificationBody);

  @PUT("/v1/users/self")
  Observable<Response<User>> updateUserSettings(@Body SettingsBody body);
}
