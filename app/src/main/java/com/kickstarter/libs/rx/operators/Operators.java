package com.kickstarter.libs.rx.operators;

import com.google.gson.Gson;
import com.kickstarter.services.ApiException;
import com.kickstarter.services.ResponseException;

import androidx.annotation.NonNull;
import rx.Subscriber;

public final class Operators {
  private Operators() {}

  /**
   * When a response errors, send an {@link ApiException} or {@link ResponseException} to
   * {@link Subscriber#onError}, otherwise send the response to {@link Subscriber#onNext}.
   */
  public static @NonNull <T> ApiErrorOperator<T> apiError(final @NonNull Gson gson) {
    return new ApiErrorOperator<>(gson);
  }
}
