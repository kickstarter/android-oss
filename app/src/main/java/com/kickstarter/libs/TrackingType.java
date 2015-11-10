package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Map;

public interface TrackingType {
  void track(String eventName);
  void trackMap(String eventName, Map<String, Object> properties);

  final class Api implements TrackingType {
    @NonNull private final MixpanelAPI mixpanel;
    public Api(@ForApplication @NonNull final Context context) {
      mixpanel = MixpanelAPI.getInstance(context, "koala");
    }

    @Override
    public void track(String eventName) {
      mixpanel.track(eventName);
    }

    @Override
    public void trackMap(String eventName, Map<String, Object> properties) {
      mixpanel.trackMap(eventName, properties);
    }
  }

  final class NoOp implements TrackingType {
    @Override public void track(String eventName) {}
    @Override public void trackMap(String eventName, Map<String, Object> properties) {}
  }
}
