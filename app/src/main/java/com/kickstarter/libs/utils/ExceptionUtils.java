package com.kickstarter.libs.utils;

import androidx.annotation.NonNull;

public final class ExceptionUtils {
  private ExceptionUtils() {}

  /**
   * Rethrows `exception` if it's already a {@link RuntimeException}, otherwise it wraps `exception`
   * in a {@link RuntimeException} and then rethrows.
   *
   * I would be remiss if I didn't mention that this is basically a "flatMap" operation ;)
   */
  public static void rethrowAsRuntimeException(final @NonNull Exception exception) {
    if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    }
    throw new RuntimeException(exception);
  }
}
