package com.kickstarter.libs;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.kickstarter.R;
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
  private final @NonNull Context context;

  private final @NonNull MixpanelAPI mixpanel;

  // Cached values
  private @Nullable Boolean isGooglePlayServicesAvailable = null;

  public KoalaTrackingClient(@ForApplication @NonNull final Context context, @NonNull final CurrentUser currentUser) {
    this.context = context;
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
      put("android_play_services_available", isGooglePlayServicesAvailable());
      put("client_platform", "android");
      put("device_orientation", deviceOrientation());
      put("device_format", deviceFormat());
      put("device_fingerprint", mixpanel.getDistinctId());
      put("android_uuid", mixpanel.getDistinctId());

      // TODO: any way to detect if android pay is available?
      // put("android_pay_capable", false);
    }};
  }

  /**
   * Derives the device's orientation (portrait/landscape) from the `context`.
   */
  private @NonNull String deviceOrientation() {
    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return "landscape";
    }
    return "portrait";
  }

  private @NonNull String deviceFormat() {
    return context.getResources().getBoolean(R.bool.isTablet) ? "tablet" : "phone";
  }

  /**
   * Derives the availability of google play services from the `context`.
   */
  private boolean isGooglePlayServicesAvailable() {
    if (isGooglePlayServicesAvailable == null) {
      isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.context.getApplicationContext()) != ConnectionResult.SUCCESS;
    }
    return isGooglePlayServicesAvailable;
  }
}
