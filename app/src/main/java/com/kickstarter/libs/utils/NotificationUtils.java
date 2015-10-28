package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.libs.gcm.RegistrationService;

public class NotificationUtils {
  public NotificationUtils() {}

  public static void registerDevice(@NonNull final Context context) {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    // Start IntentService to register this application with GCM.
    final Intent intent = new Intent(context, RegistrationService.class);
    context.startService(intent);
  }

  public static void unregisterDevice(@NonNull final Context context) {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    // TODO
  }
}
