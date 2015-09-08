package com.kickstarter.services;

import com.kickstarter.models.Project;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.ApiResponses.ActivityEnvelope;
import com.kickstarter.services.ApiResponses.DiscoverEnvelope;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import rx.Observable;

/*package*/ interface ApiService {
  @GET("/v1/activities")
  Observable<ActivityEnvelope> fetchActivities(@Query("categories[]") List<String> categories);

  @GET("/v1/discover")
  Observable<DiscoverEnvelope> fetchProjects(@QueryMap Map<String, String> params);

  @GET("/v1/projects/{param}")
  public Observable<Project> fetchProject(@Path("param") String param);

  @POST("/xauth/access_token")
  public Observable<AccessTokenEnvelope> login(@Query("email") String email,
    @Query("password") String password);

  @POST("/xauth/access_token")
  public Observable<AccessTokenEnvelope> login(@Query("email") String email,
    @Query("password") String password,
    @Query("code") String code);
}
