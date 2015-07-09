package com.kickstarter.services;

import android.util.Base64;

import com.google.gson.GsonBuilder;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.Build;
import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;

import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class KickstarterClient {
  private final Build build;
  private final Endpoint endpoint;
  private final KickstarterService service;

  public KickstarterClient(final Build build, final Endpoint endpoint) {
    this.build = build;
    this.endpoint = endpoint;
    service = kickstarterService();
  }

  private KickstarterService kickstarterService() {
    return restAdapter().create(KickstarterService.class);
  }

  public Observable<InternalBuildEnvelope> pingBeta() {
    return service.pingBeta();
  }

  private RestAdapter restAdapter() {
    return new RestAdapter.Builder()
      .setConverter(gsonConverter())
      .setEndpoint(endpoint)
      .setRequestInterceptor(requestInterceptor())
      .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
      .build();
  }

  private GsonConverter gsonConverter() {
    return new GsonConverter(new GsonBuilder().create());
  }

  private RequestInterceptor requestInterceptor() {
    return request -> {
      request.addHeader("Accept", "application/json");
      request.addHeader("Kickstarter-Android-App", build.versionCode().toString());

      // Check if the URL contains kickstarter.com, but not www - if so, it's almost
      // certainly a Hivequeen environment with basic auth in front of it.
      if (endpoint.getUrl().matches("\\Ahttps:\\/\\/(?=.*kickstarter.com)(?!.*www).*")) {
        request.addHeader("Authorization", "Basic ZnV6enk6d3V6enk=");
      }

      // TODO: Check whether device is mobile or tablet, append to user agent
      final StringBuilder userAgent = new StringBuilder()
        .append("Kickstarter Android Mobile Variant/")
        .append(build.variant())
        .append(" Code/")
        .append(build.versionCode())
        .append(" Version/")
        .append(build.versionName());
      request.addHeader("User-Agent", userAgent.toString());
    };
  }
}
