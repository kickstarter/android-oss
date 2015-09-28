package com.kickstarter.services;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.Build;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class KickstarterClient {
  private final Build build;
  private final String endpoint;
  private final Gson gson;
  private final KickstarterService service;

  public KickstarterClient(final Build build, final Gson gson, final String endpoint) {
    this.build = build;
    this.gson = gson;
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
