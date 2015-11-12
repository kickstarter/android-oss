package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.User;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public final class KoalaTrackingClient implements TrackingClientType {
  @Inject CurrentUser currentUser;
  @Nullable private User loggedInUser;

  @NonNull private final MixpanelAPI mixpanel;

  public KoalaTrackingClient(@ForApplication @NonNull final Context context, @NonNull final CurrentUser currentUser) {
    this.currentUser = currentUser;

    // Cache the most recent logged in user for default Koala properties.
    this.currentUser.observable().subscribe(u -> loggedInUser = u);

    mixpanel = MixpanelAPI.getInstance(context, "koala");
  }

  @Override
  public void track(@NonNull final String eventName) {
    track(eventName, new HashMap<>());
  }

  @Override
  public void track(@NonNull final String eventName, @NonNull final Map<String, Object> properties) {
    final Map<String, Object> newProperties = new HashMap<>(properties);
    newProperties.putAll(defaultProperties());

    mixpanel.trackMap(eventName, MapUtils.compact(newProperties));
  }

  @NonNull
  @Override
  public Map<String, Object> defaultProperties() {

    return new HashMap<String, Object>() {{
      if (loggedInUser != null) {
        putAll(KoalaUtils.userProperties(loggedInUser));
      }

      put("client_type", "native");
      // TODO: can we detect phone app running on tablets?
      put("device_format", "phone");
      put("client_platform", "android");

      // TODO: any equivalent to iOS's UIDevice.currentDevice.identifierForVendor.UUIDString?
      // put("device_fingerprint", "deadbeef");
      // TODO: same value as above
      //put("android_uuid", "deadbeef");

      // TODO: any way to detect if android pay is available?
      // put("android_pay_capable", false);

      // TODO: any way to detect if play services is available?
      // put("android_play_services_available", false);

      // TODO: any way to detect device orientaiton?
      // put("device_orientation", "portrait");
    }};
  }
}
