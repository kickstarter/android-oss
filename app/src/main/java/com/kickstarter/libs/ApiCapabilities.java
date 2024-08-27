package com.kickstarter.libs;

import android.os.Build;

public final class ApiCapabilities {
  private ApiCapabilities() {}

  public static boolean canCreateNotificationChannels() {
    return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
  }
}
