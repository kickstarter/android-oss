package com.kickstarter.services;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

/**
 * An exception class wrapping an error envelope.
 */
public final class ApiError extends RuntimeException {
  private final ErrorEnvelope errorEnvelope;

  public ApiError(@NonNull final ErrorEnvelope errorEnvelope) {
    this.errorEnvelope = errorEnvelope;
  }

  public ErrorEnvelope errorEnvelope() {
    return errorEnvelope;
  }
}
