package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.ui.activities.DiscoveryActivity;

public final class ApplicationUtils {
  private ApplicationUtils() {}

  /**
   *
   * Creates an intent for starting the main activity at the top of a task stack, clearing all previous activities.
   *
   * `ACTION_MAIN` does not expect to receive any data in the intent, it should be the same intent as if a user had
   * just launched the app.
   */
  public static void restartActionMain(final @NonNull Context context) {
    final Intent intent = new Intent(context, DiscoveryActivity.class)
      .setAction(Intent.ACTION_MAIN)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

    context.startActivity(intent);
  }
}
