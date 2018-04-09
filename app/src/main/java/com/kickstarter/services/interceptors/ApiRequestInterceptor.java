package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.services.KSUri;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class ApiRequestInterceptor implements Interceptor {
  private final String clientId;
  private final CurrentUserType currentUser;
  private final String endpoint;

  public ApiRequestInterceptor(final @NonNull String clientId, final @NonNull CurrentUserType currentUser,
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
    Log.d(ApiRequestInterceptor.class.getSimpleName(), "testtest " + initialRequest.url().toString() + " " + initialRequest.header("User-Agent"));

    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    Request accept = initialRequest.newBuilder()
      .header("Accept", "application/json")
      .url(url(initialRequest.url()))
      .build();
    Log.d(ApiRequestInterceptor.class.getSimpleName(), "testtest " + accept.url().toString() + " " + accept.header("User-Agent"));

    return accept;
  }

  private HttpUrl url(final @NonNull HttpUrl initialHttpUrl) {
    final HttpUrl.Builder builder = initialHttpUrl.newBuilder()
      .setQueryParameter("client_id", this.clientId);
    if (this.currentUser.exists()) {
      builder.setQueryParameter("oauth_token", this.currentUser.getAccessToken());
    }

    return builder.build();
  }

  private boolean shouldIntercept(final @NonNull Request request) {
    return KSUri.isApiUri(Uri.parse(request.url().toString()), this.endpoint);
  }
}
