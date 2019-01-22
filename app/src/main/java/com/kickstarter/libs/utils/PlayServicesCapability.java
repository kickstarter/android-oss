package com.kickstarter.libs.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kickstarter.KSApplication;

import androidx.annotation.NonNull;

public final class PlayServicesCapability {
  private final boolean isCapable;

  public PlayServicesCapability(final boolean isCapable) {
    this.isCapable = isCapable;
  }

  public PlayServicesCapability(final @NonNull Context context) {
    final KSApplication application = (KSApplication) context.getApplicationContext();
    if (application.isInUnitTests()) {
      this.isCapable = false;
    } else {
      final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
      final int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
      this.isCapable = resultCode == ConnectionResult.SUCCESS;
    }
  }

  /**
   * Check the device to make sure it has the Google Play Services APK.
   */
  public boolean isCapable() {
    return this.isCapable;
  }
}
