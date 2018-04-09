package com.kickstarter.services.interceptors;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUserType;
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
  private final @NonNull CurrentUserType currentUser;
  private final @NonNull String endpoint;
  private final @NonNull InternalToolsType internalTools;
  private final @NonNull Build build;
  private final @NonNull AndroidPayCapability androidPayCapability;

  public WebRequestInterceptor(final @NonNull CurrentUserType currentUser, final @NonNull String endpoint,
    final InternalToolsType internalTools, final @NonNull Build build,
    final @NonNull AndroidPayCapability androidPayCapability) {
    this.currentUser = currentUser;
    this.endpoint = endpoint;
    this.internalTools = internalTools;
    this.build = build;
    this.androidPayCapability = androidPayCapability;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(final @NonNull Request initialRequest) {
    Log.d(WebRequestInterceptor.class.getSimpleName(), "testtest " + initialRequest.url().toString() + " " + initialRequest.header("User-Agent"));

    if (!shouldIntercept(initialRequest)) {
      return initialRequest;
    }

    final Request.Builder requestBuilder = initialRequest.newBuilder()
      .header("User-Agent", userAgent(this.build));

    final String basicAuthorizationHeader = this.internalTools.basicAuthorizationHeader();
    if (this.currentUser.exists()) {
      requestBuilder.addHeader("Authorization", "token " + this.currentUser.getAccessToken());
    } else if (shouldAddBasicAuthorizationHeader(initialRequest) && isNotNull(basicAuthorizationHeader)) {
      requestBuilder.addHeader("Authorization", basicAuthorizationHeader);
    }
    if (this.androidPayCapability.isCapable()) {
      requestBuilder.addHeader("Kickstarter-Android-Pay", "1");
    }

    Request build = requestBuilder.build();
    Log.d(WebRequestInterceptor.class.getSimpleName(), "testtest " + build.url().toString() + " " + build.header("User-Agent"));
    return build;
  }

  private boolean shouldIntercept(final @NonNull Request request) {
    return KSUri.isWebUri(Uri.parse(request.url().toString()), this.endpoint);
  }

  private boolean shouldAddBasicAuthorizationHeader(final @NonNull Request request) {
    if (this.currentUser.exists()) {
      return false;
    }
    final Uri initialRequestUri = Uri.parse(request.url().toString());
    return KSUri.isHivequeenUri(initialRequestUri, this.endpoint) || KSUri.isStagingUri(initialRequestUri, this.endpoint);
  }

  public static @NonNull String userAgent(final @NonNull Build build) {
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

