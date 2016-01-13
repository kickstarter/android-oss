package com.kickstarter.libs.utils;

public final class ProgressBarUtils {
  private ProgressBarUtils() {}

  /**
   * Clamps a value and converts it to an integer, ready for use by setProgress.
   */
  public static int progress(final float value) {
    return Math.round(Math.min(100.0f, value));
  }
}
