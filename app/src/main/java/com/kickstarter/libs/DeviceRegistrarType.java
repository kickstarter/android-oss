package com.kickstarter.libs;

public interface DeviceRegistrarType {
  /**
   * Call a service to register the device with Google Cloud Messaging.
   */
  void registerDevice();

  /**
   * Call a service to unregister the device with Google Cloud Messaging.
   */
  void unregisterDevice();
}
