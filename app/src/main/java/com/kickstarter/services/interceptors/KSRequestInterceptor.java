package com.kickstarter.services.interceptors;

import com.kickstarter.libs.Build;
import com.kickstarter.libs.perimeterx.PerimeterXClientType;
import com.kickstarter.libs.utils.I18nUtils;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to apply to all outgoing requests.
 */
public final class KSRequestInterceptor implements Interceptor {
  private final Build build;
  private final PerimeterXClientType pxManager;

  public KSRequestInterceptor(final @NonNull Build build, final @NonNull PerimeterXClientType manager) {
    this.build = build;
    this.pxManager = manager;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(final @NonNull Request initialRequest) {
    final Request.Builder builder = initialRequest.newBuilder()
            .header("Kickstarter-Android-App", this.build.versionCode().toString())
            .header("Kickstarter-App-Id", this.build.applicationId())
            .header("Accept-Language", I18nUtils.language());

    this.pxManager.addHeaderTo(builder);
    return builder.build();
  }
}
