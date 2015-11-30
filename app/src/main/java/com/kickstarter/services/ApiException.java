package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

/**
 * An exception class wrapping an {@link ErrorEnvelope}.
 */
public final class ApiException extends ResponseException {
  private final ErrorEnvelope errorEnvelope;

  public ApiException(@NonNull final ErrorEnvelope errorEnvelope, @NonNull final retrofit.Response response) {
    super(response);
    this.errorEnvelope = errorEnvelope;
  }

  public @NonNull ErrorEnvelope errorEnvelope() {
    return errorEnvelope;
  }
}
