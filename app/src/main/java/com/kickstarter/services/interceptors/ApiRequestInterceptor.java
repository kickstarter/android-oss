package com.kickstarter.services.interceptors;

import android.net.Uri;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.perimeterx.PerimeterXClientType;
import com.kickstarter.libs.utils.WebUtils;
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
  private final PerimeterXClientType pxManager;
  private final Build build;

  public ApiRequestInterceptor(final @NonNull String clientId, final @NonNull CurrentUserType currentUser,
                               final @NonNull String endpoint, final @NonNull PerimeterXClientType manager,
                               final @NonNull Build build) {
    this.clientId = clientId;
    this.currentUser = currentUser;
    this.endpoint = endpoint;
    this.pxManager = manager;
    this.build = build;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    final Response response = chain.proceed(request(chain.request()));
    this.pxManager.intercept(response);
    return response;
  }

  private Request request(final @NonNull Request initialRequest) {
    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    final Request.Builder builder = initialRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Kickstarter-Android-App-UUID", FirebaseInstanceId.getInstance().getId())
            .addHeader("User-Agent", WebUtils.INSTANCE.userAgent(this.build));

    this.pxManager.addHeaderTo(builder);

    return builder
      .url(url(initialRequest.url()))
      .build();
  }

  private HttpUrl url(final @NonNull HttpUrl initialHttpUrl) {
    final HttpUrl.Builder builder = initialHttpUrl.newBuilder()
      .setQueryParameter("client_id", this.clientId);

    this.currentUser.observable()
      .subscribe(user -> builder.setQueryParameter("oauth_token", this.currentUser.getAccessToken()));

    return builder.build();
  }

  private boolean shouldIntercept(final @NonNull Request request) {
    return KSUri.isApiUri(Uri.parse(request.url().toString()), this.endpoint);
  }
}
