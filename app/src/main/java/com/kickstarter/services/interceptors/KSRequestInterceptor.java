package com.kickstarter.services.interceptors;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Release;
import com.kickstarter.libs.utils.I18nUtils;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Interceptor to apply to all outgoing requests.
 */
public final class KSRequestInterceptor implements Interceptor {
  final Release release;

  public KSRequestInterceptor(@NonNull final Release release) {
    this.release = release;
  }

  @Override
  public Response intercept(@NonNull final Chain chain) throws IOException {
    return chain.proceed(request(chain.request()));
  }

  private Request request(@NonNull final Request initialRequest) {
    return initialRequest.newBuilder()
      .header("Kickstarter-Android-App", release.versionCode().toString())
      .header("Kickstarter-App-Id", release.applicationId())
      .header("Accept-Language", I18nUtils.language())
      .build();
  }
}
