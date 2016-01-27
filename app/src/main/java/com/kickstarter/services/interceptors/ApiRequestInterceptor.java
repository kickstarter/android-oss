package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.libs.CurrentUser;
import com.kickstarter.services.KSUri;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public final class ApiRequestInterceptor implements Interceptor {
  final String clientId;
  final CurrentUser currentUser;
  final String endpoint;

  public ApiRequestInterceptor(final @NonNull String clientId, final @NonNull CurrentUser currentUser,
    final @NonNull String endpoint) {
    this.clientId = clientId;
    this.currentUser = currentUser;
    this.endpoint = endpoint;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(final @NonNull Request initialRequest) {
    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    return initialRequest.newBuilder()
      .header("Accept", "application/json")
      .url(url(initialRequest.httpUrl()))
      .build();
  }

  private HttpUrl url(final @NonNull HttpUrl initialHttpUrl) {
    final HttpUrl.Builder builder = initialHttpUrl.newBuilder()
      .setQueryParameter("client_id", clientId);
    if (currentUser.exists()) {
      builder.setQueryParameter("oauth_token", currentUser.getAccessToken());
    }

    return builder.build();
  }

  private boolean shouldIntercept(final @NonNull Request request) {
    return KSUri.isApiUri(Uri.parse(request.urlString()), endpoint);
  }
}
