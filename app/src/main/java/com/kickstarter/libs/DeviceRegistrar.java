package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.PlayServicesCapability;
import com.kickstarter.services.firebase.RegisterService;
import com.kickstarter.services.firebase.UnregisterService;

public final class DeviceRegistrar implements DeviceRegistrarType {
  private final @NonNull PlayServicesCapability playServicesCapability;
  private @ApplicationContext @NonNull Context context;

  public static final String TOPIC_GLOBAL = "global";

  public DeviceRegistrar(final @NonNull PlayServicesCapability playServicesCapability,
    final @ApplicationContext @NonNull Context context) {
    this.playServicesCapability = playServicesCapability;
    this.context = context;
  }

  /**
   * If Play Services is available on this device, start a service to register it with Google Cloud Messaging.
   */
  public void registerDevice() {
    if (!this.playServicesCapability.isCapable()) {
      return;
    }

    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this.context));
    Job job = dispatcher.newJobBuilder()
      .setService(RegisterService.class)
      .setTag("Register-service")
      .build();

    dispatcher.mustSchedule(job);
  }

  /**
   * If Play Services is available on this device, start a service to unregister it with Google Cloud Messaging.
   */
  public void unregisterDevice() {
    if (!this.playServicesCapability.isCapable()) {
      return;
    }

    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this.context));
    Job job = dispatcher.newJobBuilder()
      .setService(UnregisterService.class)
      .setTag("Unregister-service")
      .build();

    dispatcher.mustSchedule(job);
  }
}
