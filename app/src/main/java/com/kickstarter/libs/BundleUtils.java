package com.kickstarter.libs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BundleUtils {
  private BundleUtils() {}

  public static Bundle maybeGetBundle(@Nullable final Bundle state, @NonNull final String key) {
    if (state == null) {
      return null;
    }

    return state.getBundle(key);
  }
}
