package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Release;
import com.kickstarter.services.KSUri;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Interceptor to apply to all outgoing requests.
 */
public final class KSRequestInterceptor implements Interceptor {
  final CurrentUser currentUser;
  final Release release;

  public KSRequestInterceptor(@NonNull final CurrentUser currentUser, @NonNull final Release release) {
    this.currentUser = currentUser;
    this.release = release;
  }

  @Override
  public Response intercept(@NonNull final Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(@NonNull final Request request) {
    return request.newBuilder()
      .header("Kickstarter-Android-App", release.versionCode().toString())
      .header("Kickstarter-App-Id", release.applicationId())
      .url(httpUrl(request.httpUrl()))
      .build();
  }

  private HttpUrl httpUrl(@NonNull final HttpUrl httpUrl) {
    return httpUrl.newBuilder()
      .addQueryParameter("oauth_token", currentUser.getAccessToken())
      .build();
  }
}
