package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.Release;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class WebClient {
  private final Release build;
  private final String endpoint;
  private final Gson gson;
  private final WebService service;

  public WebClient(@NonNull final Release build, @NonNull final Gson gson, @NonNull final String endpoint) {
    this.build = build;
    this.gson = gson;
    this.endpoint = endpoint;
    service = webService();
  }

  private WebService webService() {
    return restAdapter().create(WebService.class);
  }

  public Observable<InternalBuildEnvelope> pingBeta() {
    return service.pingBeta();
  }

  private RestAdapter restAdapter() {
    return new RestAdapter.Builder()
      .setConverter(gsonConverter())
      .setEndpoint(endpoint)
      .setRequestInterceptor(requestInterceptor())
      .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.HEADERS_AND_ARGS : RestAdapter.LogLevel.NONE)
      .build();
  }

  private GsonConverter gsonConverter() {
    return new GsonConverter(gson);
  }

  private RequestInterceptor requestInterceptor() {
    return request -> {
      request.addHeader("Accept", "application/json");
      request.addHeader("Kickstarter-Android-App", build.versionCode().toString());

      // Add authorization if it's a Hivequeen environment.
      final Matcher matcher = Pattern.compile("\\Ahttps:\\/\\/([a-z]+)\\.***REMOVED***\\z")
        .matcher(endpoint);
      if (matcher.matches() && !matcher.group(1).equals("www")) {
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
