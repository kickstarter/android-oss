package com.kickstarter.libs;

import android.os.Bundle;

public class BundleUtils {
  private BundleUtils() {}

  public static Bundle maybeGetBundle(final Bundle state, final String key) {
    if (state == null) {
      return null;
    }

    return state.getBundle(key);
  }
}
