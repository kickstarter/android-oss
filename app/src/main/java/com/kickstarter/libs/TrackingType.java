package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kickstarter.libs.utils.MapUtils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Map;

public interface TrackingType {
  void track(final String eventName);
  void track(final String eventName, final Map<String, Object> properties);
  @NonNull Map<String, Object> defaultProperties();

  final class Api implements TrackingType {
    @NonNull private final MixpanelAPI mixpanel;
    public Api(@ForApplication @NonNull final Context context) {
      mixpanel = MixpanelAPI.getInstance(context, "koala");
    }

    @Override
    public void track(@NonNull final String eventName) {
      track(eventName, new HashMap<>());
    }

    @Override
    public void track(@NonNull final String eventName, @NonNull final Map<String, Object> properties) {
      final Map<String, Object> newProperties = MapUtils.compact(properties);
      newProperties.putAll(defaultProperties());

      mixpanel.trackMap(eventName, newProperties);
    }

    @NonNull
    @Override
    public Map<String, Object> defaultProperties() {
      return new HashMap<String, Object>() {{
        put("client_type", "native");
        // TODO: can we detect phone app running on tablets?
        put("device_format", "phone");
        put("client_platform", "android");

        // TODO: any equivalent to iOS's UIDevice.currentDevice.identifierForVendor.UUIDString?
        // put("device_fingerprint", "deadbeef");
        // TODO: same value as above
        put("android_uuid", "deadbeef");

        // TODO: any way to detect device orientaiton?
        // put("device_orientation", "portrait");
      }};
    }
  }

  final class NoOp implements TrackingType {
    @Override public void track(String eventName) {}
    @Override public void track(String eventName, Map<String, Object> properties) {}

    @NonNull
    @Override
    public Map<String, Object> defaultProperties() {
      return new HashMap<>();
    }
  }
}
