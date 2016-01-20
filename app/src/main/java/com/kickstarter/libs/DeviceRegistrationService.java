package com.kickstarter.libs;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.libs.utils.PlayServicesUtils;
import com.kickstarter.services.gcm.RegisterService;
import com.kickstarter.services.gcm.UnregisterService;

public final class DeviceRegistrationService implements DeviceRegistrationServiceType {
  private @ForApplication @NonNull Context context;

  public DeviceRegistrationService(final @ForApplication @NonNull Context context) {
    this.context = context;
  }

  /**
   * If Play Services is available on this device, start a service to register it with Google Cloud Messaging.
   */
  public void registerDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    context.startService(new Intent(context, RegisterService.class));
  }

  /**
   * If Play Services is available on this device, start a service to unregister it with Google Cloud Messaging.
   */
  public void unregisterDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    context.startService(new Intent(context, UnregisterService.class));
  }
}
