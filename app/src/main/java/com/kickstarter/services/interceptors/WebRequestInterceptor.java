package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.libs.Release;
import com.kickstarter.services.KSUri;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public final class WebRequestInterceptor implements Interceptor {
  final String endpoint;
  final Release release;

  public WebRequestInterceptor(@NonNull final String endpoint, @NonNull final Release release) {
    this.endpoint = endpoint;
    this.release = release;
  }

  @Override
  public Response intercept(@NonNull final Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(@NonNull final Request request) {
    if (!shouldIntercept(request)) {
      return request;
    }

    Request.Builder requestBuilder = request.newBuilder()
      .addHeader("User-Agent", userAgent());
    if (KSUri.isHivequeenUri(Uri.parse(request.urlString()), endpoint)) {
      requestBuilder = requestBuilder.header("Authorization", "Basic ZnV6enk6d3V6enk=");
    }
    return requestBuilder.build();
  }

  private boolean shouldIntercept(@NonNull final Request request) {
    return KSUri.isWebUri(Uri.parse(request.urlString()), endpoint);
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

