package com.kickstarter.services;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.services.apirequests.CommentBody;
import com.kickstarter.services.apirequests.PushTokenBody;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CategoriesEnvelope;
import com.kickstarter.services.apiresponses.CommentsEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;

import java.util.List;
import java.util.Map;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import rx.Observable;

/*package*/ interface ApiService {
  @GET("/v1/activities")
  Observable<ActivityEnvelope> fetchActivities(@Query("categories[]") List<String> categories);

  @GET("/v1/categories")
  Observable<CategoriesEnvelope> fetchCategories();

  @GET("/v1/projects/{project_param}/backers/{user_param}")
  Observable<Backing> fetchProjectBacking(
    @Path("project_param") String projectParam,
    @Path("user_param") String userParam
  );

  @GET("/v1/projects/{param}/comments")
  Observable<CommentsEnvelope> fetchProjectComments(@Path("param") String param);

  @GET("/v1/discover")
  Observable<DiscoverEnvelope> fetchProjects(@QueryMap Map<String, String> params);

  @GET("/v1/projects/{param}")
  Observable<Project> fetchProject(@Path("param") String param);

  @GET("/v1/categories/{id}")
  Observable<Category> fetchCategory(@Path("id") long id);

  @POST("/xauth/access_token")
  Observable<AccessTokenEnvelope> login(@Query("email") String email,
    @Query("password") String password);

  @POST("/xauth/access_token")
  Observable<AccessTokenEnvelope> login(@Query("email") String email,
    @Query("password") String password,
    @Query("code") String code);

  @POST("/v1/projects/{param}/comments/")
  Observable<Comment> postProjectComment(@Path("param") String param, @Body CommentBody body);

  @POST("/v1/users/self/push_tokens")
  Observable<Empty> registerPushToken(@Body PushTokenBody body);

  @PUT("/v1/projects/{param}/star")
  Observable<StarEnvelope> starProject(@Path("param") String param);

  @POST("/v1/projects/{param}/star/toggle")
  Observable<StarEnvelope> toggleProjectStar(@Path("param") String param);
}
