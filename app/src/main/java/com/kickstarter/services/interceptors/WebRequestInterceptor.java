package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Release;
import com.kickstarter.services.KSUri;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Interceptor for web requests to Kickstarter, not API requests. Used by web views and the web client.
 */
public final class WebRequestInterceptor implements Interceptor {
  final CurrentUser currentUser;
  final String endpoint;
  final Release release;

  public WebRequestInterceptor(@NonNull final CurrentUser currentUser, @NonNull final String endpoint,
    @NonNull final Release release) {
    this.currentUser = currentUser;
    this.endpoint = endpoint;
    this.release = release;
  }

  @Override
  public Response intercept(@NonNull final Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(@NonNull final Request initialRequest) {
    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    final Request.Builder requestBuilder = initialRequest.newBuilder()
      .header("User-Agent", userAgent());

    if (shouldAddBasicAuthorizationHeader(initialRequest)) {
      requestBuilder.addHeader("Authorization", "Basic ZnV6enk6d3V6enk=");
    }
    if (currentUser.exists()) {
      requestBuilder.addHeader("Authorization", "token " + currentUser.getAccessToken());
    }

    return requestBuilder.build();
  }

  private boolean shouldIntercept(@NonNull final Request request) {
    return KSUri.isWebUri(Uri.parse(request.urlString()), endpoint);
  }

  private boolean shouldAddBasicAuthorizationHeader(@NonNull final Request request) {
    final Uri initialRequestUri = Uri.parse(request.urlString());
    return KSUri.isHivequeenUri(initialRequestUri, endpoint) || KSUri.isStagingUri(initialRequestUri, endpoint);
  }

  private @NonNull String userAgent() {
    // TODO: Check whether device is mobile or tablet, append to user agent
    return new StringBuilder()
      .append("Kickstarter Android Mobile Variant/")
      .append(release.variant())
      .append(" Code/")
      .append(release.versionCode())
      .append(" Version/")
      .append(release.versionName())
      .toString();
  }
}

