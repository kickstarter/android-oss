package com.kickstarter.services.interceptors;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Release;
import com.kickstarter.libs.utils.I18nUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to apply to all outgoing requests.
 */
public final class KSRequestInterceptor implements Interceptor {
  final Release release;

  public KSRequestInterceptor(final @NonNull Release release) {
    this.release = release;
  }

  @Override
  public Response intercept(final @NonNull Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(final @NonNull Request initialRequest) {
    return initialRequest.newBuilder()
      .header("Kickstarter-Android-App", release.versionCode().toString())
      .header("Kickstarter-App-Id", release.applicationId())
      .header("Accept-Language", I18nUtils.language())
      .build();
  }
}
