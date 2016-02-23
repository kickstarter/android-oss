package com.kickstarter.libs;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public abstract class TrackingClientType {
  public abstract void track(final String eventName, final Map<String, Object> properties);

  public final void track(final String eventName) {
    track(eventName, new HashMap<>());
  }

  public @NonNull Map<String, Object> defaultProperties() {
    return new HashMap<>();
  }
}
