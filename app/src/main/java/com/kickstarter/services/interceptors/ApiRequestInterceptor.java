package com.kickstarter.services.interceptors;

import android.net.Uri;

import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.services.KSUri;

import java.io.IOException;

import androidx.annotation.NonNull;
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
    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    return initialRequest.newBuilder()
      .header("Accept", "application/json")
      .url(url(initialRequest.url()))
      .build();
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
