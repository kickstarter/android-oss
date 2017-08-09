package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

/**
 * An exception class wrapping an {@link ErrorEnvelope}.
 */
public final class ApiException extends ResponseException {
  private final ErrorEnvelope errorEnvelope;

  public ApiException(final @NonNull ErrorEnvelope errorEnvelope, final @NonNull retrofit2.Response response) {
    super(response);
    this.errorEnvelope = errorEnvelope;
  }

  public @NonNull ErrorEnvelope errorEnvelope() {
    return this.errorEnvelope;
  }
}
