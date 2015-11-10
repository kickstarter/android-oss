package com.kickstarter.libs.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import timber.log.Timber;

public class PlayServicesUtils {
  public PlayServicesUtils() {}

  /**
   * Check the device to make sure it has the Google Play Services APK.
   */
  public static boolean isAvailable(@NonNull final Context context) {
    final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    final int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
    if (resultCode != ConnectionResult.SUCCESS) {
      Timber.d("This device does not have Google Play Services installed.");
      return false;
    }
    return true;
  }
}
