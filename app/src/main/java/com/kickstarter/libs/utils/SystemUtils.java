package com.kickstarter.libs.utils;

public final class SystemUtils {
  private SystemUtils() {}

  /**
   * Returns the number of seconds since the epoch (1/1/1970).
   */
  public static long secondsSinceEpoch() {
    return System.currentTimeMillis() / 1000l;
  }
}
