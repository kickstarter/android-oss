package com.kickstarter.libs.utils;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class BundleUtils {
  private BundleUtils() {}

  public static Bundle maybeGetBundle(final @Nullable Bundle state, final @NonNull String key) {
    if (state == null) {
      return null;
    }

    return state.getBundle(key);
  }
}
