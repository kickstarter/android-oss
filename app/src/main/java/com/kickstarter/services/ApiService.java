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
import com.kickstarter.services.apirequests.CommentBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.NotificationBody;
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
import com.kickstarter.services.apiresponses.StarEnvelope;

import java.util.List;
import java.util.Map;

import retrofit.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import retrofit.http.Url;
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

  @GET("/v1/users/self/notifications")
  Observable<Response<List<Notification>>> notifications();

  @GET
  Observable<Response<CommentsEnvelope>> paginatedProjectComments(@Url String paginationPath);

  @POST("/v1/projects/{param}/comments/")
  Observable<Response<Comment>> postProjectComment(@Path("param") String param, @Body CommentBody body);

  @GET("/v1/projects/{project_param}/backers/{user_param}")
  Observable<Response<Backing>> projectBacking(
    @Path("project_param") String projectParam,
    @Path("user_param") String userParam
  );

  @GET("/v1/projects/{param}")
  Observable<Response<Project>> project(@Path("param") String param);

  @GET("/v1/projects/{project_param}/comments")
  Observable<Response<CommentsEnvelope>> projectComments(@Path("project_param") String projectParam);

  @GET("/v1/discover")
  Observable<Response<DiscoverEnvelope>> projects(@QueryMap Map<String, String> params);

  @GET
  Observable<Response<DiscoverEnvelope>> projects(@Url String paginationUrl);

  @POST("/v1/users/self/push_tokens")
  Observable<Response<Empty>> registerPushToken(@Body PushTokenBody body);

  @POST("/v1/users/reset")
  Observable<Response<User>> resetPassword(@Body ResetPasswordBody body);

  @POST("/v1/users")
  Observable<Response<AccessTokenEnvelope>> signup(@Body SignupBody body);

  @PUT("/v1/projects/{param}/star")
  Observable<Response<StarEnvelope>> starProject(@Path("param") String param);

  @POST("/v1/projects/{param}/star/toggle")
  Observable<Response<StarEnvelope>> toggleProjectStar(@Path("param") String param);

  @GET("/v1/projects/{project_param}/updates/{update_param}")
  Observable<Response<Update>> update(@Path("project_param") String projectParam, @Path("update_param") String updateParam);

  @PUT("/v1/users/self/notifications/{id}")
  Observable<Response<Notification>> updateProjectNotifications(@Path("id") long notificationId,
    @Body NotificationBody notificationBody);

  @PUT("/v1/users/self")
  Observable<Response<User>> updateUserSettings(@Body SettingsBody body);
}
