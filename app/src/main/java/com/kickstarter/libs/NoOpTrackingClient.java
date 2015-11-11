package com.kickstarter.libs;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

final public class NoOpTrackingClient implements TrackingClientType {
  @Override public void track(String eventName) {}
  @Override public void track(String eventName, Map<String, Object> properties) {}

  @NonNull
  @Override
  public Map<String, Object> defaultProperties() {
    return new HashMap<>();
  }
}
