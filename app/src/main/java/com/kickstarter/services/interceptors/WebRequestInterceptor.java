package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.services.KSUri;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.kickstarter.libs.utils.ObjectUtils.isNotNull;

/**
 * Interceptor for web requests to Kickstarter, not API requests. Used by web views and the web client.
 */
public final class WebRequestInterceptor implements Interceptor {
  private final @NonNull CurrentUser currentUser;
  private final @NonNull String endpoint;
  private final @NonNull InternalToolsType internalTools;
  private final @NonNull Build build;

  public WebRequestInterceptor(final @NonNull CurrentUser currentUser, final @NonNull String endpoint,
    final InternalToolsType internalTools, final @NonNull Build build) {
    this.currentUser = currentUser;
    this.endpoint = endpoint;
    this.internalTools = internalTools;
    this.build = build;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(final @NonNull Request initialRequest) {
    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    final Request.Builder requestBuilder = initialRequest.newBuilder()
      .header("User-Agent", userAgent());

    final String basicAuthorizationHeader = internalTools.basicAuthorizationHeader();
    if (shouldAddBasicAuthorizationHeader(initialRequest) && isNotNull(basicAuthorizationHeader)) {
      requestBuilder.addHeader("Authorization", basicAuthorizationHeader);
    }
    if (currentUser.exists()) {
      requestBuilder.addHeader("Authorization", "token " + currentUser.getAccessToken());
    }

    return requestBuilder.build();
  }

  private boolean shouldIntercept(final @NonNull Request request) {
    return KSUri.isWebUri(Uri.parse(request.url().toString()), endpoint);
  }

  private boolean shouldAddBasicAuthorizationHeader(final @NonNull Request request) {
    final Uri initialRequestUri = Uri.parse(request.url().toString());
    return KSUri.isHivequeenUri(initialRequestUri, endpoint) || KSUri.isStagingUri(initialRequestUri, endpoint);
  }

  private @NonNull String userAgent() {
    // TODO: Check whether device is mobile or tablet, append to user agent
    return new StringBuilder()
      .append("Kickstarter Android Mobile Variant/")
      .append(build.variant())
      .append(" Code/")
      .append(build.versionCode())
      .append(" Version/")
      .append(build.versionName())
      .toString();
  }
}

