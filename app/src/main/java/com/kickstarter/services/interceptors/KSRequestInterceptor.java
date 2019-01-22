package com.kickstarter.services.interceptors;

import com.kickstarter.libs.Build;
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

  public KSRequestInterceptor(final @NonNull Build build) {
    this.build = build;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(final @NonNull Request initialRequest) {
    return initialRequest.newBuilder()
      .header("Kickstarter-Android-App", this.build.versionCode().toString())
      .header("Kickstarter-App-Id", this.build.applicationId())
      .header("Accept-Language", I18nUtils.language())
      .build();
  }
}
