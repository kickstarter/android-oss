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
    final Map<String, Object> hashMap = new HashMap<>();

    if (this.loggedInUser != null) {
      hashMap.putAll(KoalaUtils.userProperties(this.loggedInUser));
    }

    hashMap.put("user_logged_in", this.loggedInUser != null);
    hashMap.put("client_type", "native");
    hashMap.put("android_play_services_available", isGooglePlayServicesAvailable());
    hashMap.put("client_platform", "android");
    hashMap.put("device_orientation", deviceOrientation());
    hashMap.put("device_format", deviceFormat());
    hashMap.put("device_fingerprint", this.mixpanel.getDistinctId());
    hashMap.put("android_uuid", this.mixpanel.getDistinctId());
    hashMap.put("android_pay_capable", this.androidPayCapability.isCapable());

    return hashMap;
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
