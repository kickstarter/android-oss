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

  public ApiRequestInterceptor(@NonNull final String clientId, @NonNull final CurrentUser currentUser,
    @NonNull final String endpoint) {
    this.clientId = clientId;
    this.currentUser = currentUser;
    this.endpoint = endpoint;
  }

  @Override
  public Response intercept(@NonNull final Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(@NonNull final Request initialRequest) {
    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    return initialRequest.newBuilder()
      .header("Accept", "application/json")
      .url(url(initialRequest.httpUrl()))
      .build();
  }

  private HttpUrl url(@NonNull final HttpUrl initialHttpUrl) {
    final HttpUrl.Builder builder = initialHttpUrl.newBuilder()
      .setQueryParameter("client_id", clientId);
    if (currentUser.exists()) {
      builder.setQueryParameter("oauth_token", currentUser.getAccessToken());
    }

    return builder.build();
  }

  private boolean shouldIntercept(@NonNull final Request request) {
    return KSUri.isApiUri(Uri.parse(request.urlString()), endpoint);
  }
}
