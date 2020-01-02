package com.kickstarter.libs;

import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.models.User;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public abstract class TrackingClientType {
  public abstract void track(final String eventName, final Map<String, Object> additionalProperties);

  public final void track(final String eventName) {
    track(eventName, new HashMap<>());
  }

  private @NonNull Map<String, Object> cleanProperties() {
    final Map<String, Object> hashMap = new HashMap<>();

    final boolean userIsLoggedIn = loggedInUser() != null;

    hashMap.put("time", time());
    hashMap.put("user_logged_in", userIsLoggedIn);

    return hashMap;
  }

  private @NonNull Map<String, Object> defaultProperties() {
    final Map<String, Object> hashMap = new HashMap<>();

    final boolean userIsLoggedIn = loggedInUser() != null;
    if (userIsLoggedIn) {
      hashMap.putAll(KoalaUtils.userProperties(loggedInUser()));
    }

    hashMap.put("android_uuid", androidUUID());
    hashMap.put("app_version", versionName());
    hashMap.put("brand", brand());
    hashMap.put("client_platform", "android");
    hashMap.put("client_type", "native");
    hashMap.put("device_fingerprint", androidUUID());
    hashMap.put("device_format", deviceFormat());
    hashMap.put("device_orientation", deviceOrientation());
    hashMap.put("distinct_id", androidUUID());
    hashMap.put("enabled_feature_flags", enabledFeatureFlags());
    hashMap.put("google_play_services", isGooglePlayServicesAvailable() ? "available" : "unavailable");
    hashMap.put("is_vo_on", isTalkBackOn());
    hashMap.put("koala_lib", "kickstarter_android");
    hashMap.put("manufacturer", manufacturer());
    hashMap.put("model", model());
    hashMap.put("mp_lib", "android");
    hashMap.put("os", "Android");
    hashMap.put("os_version", OSVersion());
    hashMap.put("time", time());
    hashMap.put("user_logged_in", userIsLoggedIn);

    return hashMap;
  }

  @NonNull Map<String, Object> combinedProperties(final @NonNull Map<String, Object> additionalProperties) {
    final Map<String, Object> combinedProperties = new HashMap<>(additionalProperties);
    if (cleanPropertiesOnly()) {
      combinedProperties.putAll(cleanProperties());
    } else {
      combinedProperties.putAll(defaultProperties());
    }
    return combinedProperties;
  }

  protected abstract boolean cleanPropertiesOnly();

  //Default properties
  protected abstract String androidUUID();
  protected abstract String brand();
  protected abstract String deviceFormat();
  protected abstract String deviceOrientation();
  protected abstract JSONArray enabledFeatureFlags();
  protected abstract boolean isGooglePlayServicesAvailable();
  protected abstract boolean isTalkBackOn();
  protected abstract User loggedInUser();
  protected abstract String manufacturer();
  protected abstract String model();
  protected abstract String OSVersion();
  protected abstract long time();
  protected abstract String versionName();
}
