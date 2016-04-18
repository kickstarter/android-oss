package com.kickstarter.libs;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.PlayServicesCapability;
import com.kickstarter.services.gcm.RegisterService;
import com.kickstarter.services.gcm.UnregisterService;

public final class DeviceRegistrar implements DeviceRegistrarType {
  private final @NonNull PlayServicesCapability playServicesCapability;
  private @ApplicationContext @NonNull Context context;

  public DeviceRegistrar(final @NonNull PlayServicesCapability playServicesCapability,
    final @ApplicationContext @NonNull Context context) {
    this.playServicesCapability = playServicesCapability;
    this.context = context;
  }

  /**
   * If Play Services is available on this device, start a service to register it with Google Cloud Messaging.
   */
  public void registerDevice() {
    if (!playServicesCapability.isCapable()) {
      return;
    }

    context.startService(new Intent(context, RegisterService.class));
  }

  /**
   * If Play Services is available on this device, start a service to unregister it with Google Cloud Messaging.
   */
  public void unregisterDevice() {
    if (!playServicesCapability.isCapable()) {
      return;
    }

    context.startService(new Intent(context, UnregisterService.class));
  }
}
