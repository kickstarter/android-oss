package com.kickstarter.services;

import com.google.gson.GsonBuilder;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.Build;
import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class KickstarterClient {
  private final Build build;
  private final KickstarterService service;

  public KickstarterClient(final Build build) {
    this.build = build;
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
      .setEndpoint("https://www.kickstarter.com/") // TODO: Allow endpoint switching in debug mode, ksr.10.0.3.2.xip.io for local dev
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
