package com.kickstarter.libs;

import android.support.annotation.NonNull;

import java.util.Map;

public interface TrackingClientType {
  void track(final String eventName);
  void track(final String eventName, final Map<String, Object> properties);
  @NonNull Map<String, Object> defaultProperties();
}
