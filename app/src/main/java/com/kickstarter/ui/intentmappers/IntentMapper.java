package com.kickstarter.ui.intentmappers;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A class that can be configured to inspect the data in an intent, and output data that the
 * activity/viewmodel can use to initialize itself. Activities should create instances of this
 * class and feed it any intent it encounters via the `intent()` method.
 */
public abstract class IntentMapper {

  /**
   * Attempts to extract a uri from the intent data. There may be a uri in intent data if the intent launched from a url,
   * e.g. with a deep link.
   */
  protected static @Nullable Uri uri(final @NonNull Intent intent) {
    final String string = intent.getDataString();
    if (string == null) {
      return null;
    }

    return Uri.parse(string);
  }

  public static boolean intentFromAppBanner(final @NonNull Intent intent) {
    final Uri uri = uri(intent);
    if (uri == null) {
      return false;
    } else {
      final String queryParam = uri.getQueryParameter("app_banner");
      return queryParam != null && "1".equals(queryParam);
    }
  }
}
