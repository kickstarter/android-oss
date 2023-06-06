package com.kickstarter.libs.rx.operators;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.services.ApiException;
import com.kickstarter.services.ResponseException;

public final class OperatorsV2 {
  private OperatorsV2() {}

  /**
   * When a response errors, send an {@link ApiException} or {@link ResponseException} to
   * {@link io.reactivex.Observer#onError}, otherwise send the response to {@link io.reactivex.Observer#onNext}.
   */
  public static @NonNull <T> ApiErrorOperatorV2<T> apiError(final @NonNull Gson gson) {
    return new ApiErrorOperatorV2<T>(gson);
  }
}
