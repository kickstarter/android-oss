package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Config;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apirequests.CommentBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.PushTokenBody;
import com.kickstarter.services.apirequests.RegisterWithFacebookBody;
import com.kickstarter.services.apirequests.ResetPasswordBody;
import com.kickstarter.services.apirequests.SignupBody;
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
  Observable<Response<ActivityEnvelope>> fetchActivities(@NonNull @Query("categories[]") List<String> categories);

  @GET
  Observable<Response<ActivityEnvelope>> fetchActivities(@Url @NonNull String paginationUrl);

  @GET("/v1/categories")
  Observable<Response<CategoriesEnvelope>> fetchCategories();

  @GET("/v1/projects/{project_param}/backers/{user_param}")
  Observable<Response<Backing>> fetchProjectBacking(
    @Path("project_param") String projectParam,
    @Path("user_param") String userParam
  );

  @GET("/v1/projects/{project_param}/comments")
  Observable<Response<CommentsEnvelope>> fetchProjectComments(@Path("project_param") String projectParam);

  @GET
  Observable<Response<CommentsEnvelope>> fetchPaginatedProjectComments(@Url String paginationPath);

  @GET("/v1/discover")
  Observable<Response<DiscoverEnvelope>> fetchProjects(@QueryMap Map<String, String> params);

  @GET
  Observable<Response<DiscoverEnvelope>> fetchProjects(@Url String paginationUrl);

  @GET("/v1/projects/{param}")
  Observable<Response<Project>> fetchProject(@Path("param") String param);

  @GET("/v1/categories/{id}")
  Observable<Response<Category>> fetchCategory(@Path("id") long id);

  @GET("/v1/users/self")
  Observable<Response<User>> fetchCurrentUser();

  @POST("/xauth/access_token")
  Observable<Response<AccessTokenEnvelope>> login(@Query("email") String email,
    @Query("password") String password);

  @POST("/xauth/access_token")
  Observable<Response<AccessTokenEnvelope>> login(@Query("email") String email,
    @Query("password") String password,
    @Query("code") String code);

  @PUT("/v1/facebook/access_token?intent=login")
  Observable<Response<AccessTokenEnvelope>> loginWithFacebook(@Body LoginWithFacebookBody body);

  @PUT("/v1/facebook/access_token?intent=register")
  Observable<Response<AccessTokenEnvelope>> registerWithFacebook(@Body RegisterWithFacebookBody body);

  @POST("/v1/projects/{param}/comments/")
  Observable<Response<Comment>> postProjectComment(@Path("param") String param, @Body CommentBody body);

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

  @GET("/v1/app/android/config")
  Observable<Response<Config>> config();
}
