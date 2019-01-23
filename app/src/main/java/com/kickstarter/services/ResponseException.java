package com.kickstarter.services;

import androidx.annotation.NonNull;

/**
 * An exception class wrapping a {@link retrofit2.Response}.
 */
public class ResponseException extends RuntimeException {
  private final retrofit2.Response response;

  public ResponseException(final @NonNull retrofit2.Response response) {
    this.response = response;
  }

  public @NonNull retrofit2.Response response() {
    return this.response;
  }
}
