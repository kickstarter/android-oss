package com.kickstarter.libs;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.User;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public final class KoalaTrackingClient extends TrackingClientType {
  @Inject CurrentUserType currentUser;
  @Inject AndroidPayCapability androidPayCapability;
  @Nullable private User loggedInUser;
  private final @NonNull Context context;

  private final @NonNull MixpanelAPI mixpanel;

  // Cached values
  private @Nullable Boolean isGooglePlayServicesAvailable;

  public KoalaTrackingClient(
    final @ApplicationContext @NonNull Context context,
    final @NonNull CurrentUserType currentUser,
    final @NonNull AndroidPayCapability androidPayCapability) {

    this.context = context;
    this.currentUser = currentUser;
    this.androidPayCapability = androidPayCapability;

    // Cache the most recent logged in user for default Koala properties.
    this.currentUser.observable().subscribe(u -> this.loggedInUser = u);

    this.mixpanel = MixpanelAPI.getInstance(context, "koala");
  }

  @Override
  public void track(final @NonNull String eventName, final @NonNull Map<String, Object> properties) {
    final Map<String, Object> newProperties = new HashMap<>(properties);
    newProperties.putAll(defaultProperties());

    this.mixpanel.trackMap(eventName, MapUtils.compact(newProperties));
  }

  @NonNull
  @Override
  public Map<String, Object> defaultProperties() {

    return new HashMap<String, Object>() {
      {
        if (loggedInUser != null) {
          putAll(KoalaUtils.userProperties(loggedInUser));
        }
        put("user_logged_in", loggedInUser != null);

        put("client_type", "native");
        put("android_play_services_available", isGooglePlayServicesAvailable());
        put("client_platform", "android");
        put("device_orientation", deviceOrientation());
        put("device_format", deviceFormat());
        put("device_fingerprint", mixpanel.getDistinctId());
        put("android_uuid", mixpanel.getDistinctId());
        put("android_pay_capable", androidPayCapability.isCapable());
      }
    };
  }

  /**
   * Derives the device's orientation (portrait/landscape) from the `context`.
   */
  private @NonNull String deviceOrientation() {
    if (this.context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return "landscape";
    }
    return "portrait";
  }

  private @NonNull String deviceFormat() {
    return this.context.getResources().getBoolean(R.bool.isTablet) ? "tablet" : "phone";
  }

  /**
   * Derives the availability of google play services from the `context`.
   */
  private boolean isGooglePlayServicesAvailable() {
    if (this.isGooglePlayServicesAvailable == null) {
      this.isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.context.getApplicationContext()) != ConnectionResult.SUCCESS;
    }
    return this.isGooglePlayServicesAvailable;
  }
}
