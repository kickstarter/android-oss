package com.kickstarter.services;

import android.support.annotation.NonNull;

/**
 * An exception class wrapping a {@link retrofit.Response}.
 */
public class ResponseException extends RuntimeException {
  private final retrofit.Response response;

  public ResponseException(final @NonNull retrofit.Response response) {
    this.response = response;
  }

  public @NonNull retrofit.Response response() {
    return response;
  }
}
