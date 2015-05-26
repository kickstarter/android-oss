package com.kickstarter.services;

import com.kickstarter.services.ApiResponses.ErrorEnvelope;
import retrofit.RetrofitError;


public class ApiError extends RuntimeException {
  private final RetrofitError retrofitError;
  private final ErrorEnvelope errorEnvelope;

  ApiError(final RetrofitError retrofitError, final ErrorEnvelope errorEnvelope) {
    this.retrofitError = retrofitError;
    this.errorEnvelope = errorEnvelope;
  }

  public RetrofitError retrofitError() {
    return retrofitError;
  }

  public ErrorEnvelope errorEnvelope() {
    return errorEnvelope;
  }
}
