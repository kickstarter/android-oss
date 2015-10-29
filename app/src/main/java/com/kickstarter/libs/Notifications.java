package com.kickstarter.libs;

import android.content.Context;
import android.content.Intent;

import com.kickstarter.libs.gcm.RegistrationService;
import com.kickstarter.libs.utils.PlayServicesUtils;

public class Notifications {
  @ForApplication final Context context;

  public Notifications(@ForApplication final Context context) {
    this.context = context;
  }

  public void initialize() {
    registerDevice();
  }

  public void registerDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    final Intent intent = new Intent(context, RegistrationService.class);
    context.startService(intent);
  }

  public void unregisterDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    // TODO
  }
}
