package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import retrofit.Result;

public final class ServiceUtils {
  private ServiceUtils() {}

  public static boolean isSuccessful(@NonNull final Result<?> result) {
    return !result.isError() && result.response().isSuccess();
  }
}
