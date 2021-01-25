package com.kickstarter.libs;

import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.User;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;

public abstract class TrackingClientType {
  public enum Type {
    KOALA("🐨 Koala"),
    LAKE("💧 Lake"),
    SEGMENT(" Segment");

    private String tag;

    Type(final String tag) {
      this.tag = tag;
    }

    public String getTag() {
      return this.tag;
    }
  }

  // TODO: Will add methods Screen and Identity those two are specifics to Segment, the implementation on Lake will be empty
  public abstract void track(final String eventName, final Map<String, Object> additionalProperties);

  public final void track(final String eventName) {
    track(eventName, new HashMap<>());
  }

  private @NonNull Map<String, Object> lakeProperties() {
    final Map<String, Object> hashMap = new HashMap<>();

    final boolean userIsLoggedIn = loggedInUser() != null;
    if (userIsLoggedIn) {
      hashMap.putAll(KoalaUtils.userProperties(loggedInUser()));
      hashMap.put("user_country", userCountry(loggedInUser()));
    }

    hashMap.putAll(sessionProperties(userIsLoggedIn));
    hashMap.putAll(contextProperties());

    return hashMap;
  }

  private @NonNull Map<String, Object> contextProperties() {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("timestamp", time());
      }
    };

    return MapUtils.prefixKeys(properties, "context_");
  }

  private @NonNull Map<String, Object> sessionProperties(final boolean userIsLoggedIn) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("app_build_number", buildNumber());
        put("app_release_version", versionName());
        put("client_platform", "android");
        put("client_type", "native");
        put("current_variants", currentVariants());
        put("device_distinct_id", deviceDistinctId());
        put("device_format", deviceFormat());
        put("device_manufacturer", manufacturer());
        put("device_model", model());
        put("device_orientation", deviceOrientation());
        put("display_language", Locale.getDefault().getLanguage());
        put("enabled_features", enabledFeatureFlags());
        put("is_voiceover_running", isTalkBackOn());
        put("mp_lib", "kickstarter_android");
        put("os", "Android");
        put("os_version", OSVersion());
        put("user_agent", userAgent());
        put("user_is_logged_in", userIsLoggedIn);
        put("wifi_connection", wifiConnection());
      }
    };

    return MapUtils.prefixKeys(properties, "session_");
  }

  private @NonNull Map<String, Object> koalaProperties() {
    final Map<String, Object> hashMap = new HashMap<>();

    final boolean userIsLoggedIn = loggedInUser() != null;
    if (userIsLoggedIn) {
      hashMap.putAll(KoalaUtils.userProperties(loggedInUser()));
    }

    hashMap.put("app_version", versionName());
    hashMap.put("brand", brand());
    hashMap.put("client_platform", "android");
    hashMap.put("client_type", "native");
    hashMap.put("device_fingerprint", deviceDistinctId());
    hashMap.put("device_format", deviceFormat());
    hashMap.put("device_orientation", deviceOrientation());
    hashMap.put("distinct_id", deviceDistinctId());
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
    if (type() == Type.LAKE) {
      combinedProperties.putAll(lakeProperties());
    } else if (type() == Type.KOALA){
      combinedProperties.putAll(koalaProperties());
    }
    return combinedProperties;
  }

  protected abstract Type type();
  protected abstract ExperimentsClientType optimizely();

  //Default properties
  protected abstract String brand();
  protected abstract int buildNumber();
  protected abstract JSONArray currentVariants();
  protected abstract String deviceDistinctId();
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
  protected abstract String userAgent();
  protected abstract String userCountry(User user);
  protected abstract String versionName();
  protected abstract boolean wifiConnection();
}
