package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.ui.activities.DiscoveryActivity;

public final class IntentUtils {
  /**
   * Creates an intent for starting the discovery activity at the top of the task stack.
   */
  public static @NonNull Intent discoveryIntent(final @NonNull Context context) {
    return new Intent(context, DiscoveryActivity.class)
      .setAction(Intent.ACTION_MAIN)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
  }
}
