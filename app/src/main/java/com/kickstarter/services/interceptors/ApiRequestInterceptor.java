package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.services.KSUri;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public final class ApiRequestInterceptor implements Interceptor {
  final String clientId;
  final String endpoint;

  public ApiRequestInterceptor(@NonNull final String clientId, @NonNull final String endpoint) {
    this.clientId = clientId;
    this.endpoint = endpoint;
  }

  @Override
  public Response intercept(@NonNull final Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(@NonNull final Request request) {
    if (!shouldIntercept(request)) {
      return request;
    }

    return request.newBuilder()
      .header("Accept", "application/json")
      .url(httpUrl(request.httpUrl()))
      .build();
  }

  private HttpUrl httpUrl(@NonNull final HttpUrl httpUrl) {
    return httpUrl.newBuilder()
      .addQueryParameter("client_id", clientId)
      .build();
  }

  private boolean shouldIntercept(@NonNull final Request request) {
    return KSUri.isApiUri(Uri.parse(request.urlString()), endpoint);
  }
}
