package com.kickstarter.libs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kickstarter.libs.gcm.RegistrationService;

import timber.log.Timber;

public class PlayServicesUtils {
  // TODO WHERE
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

  public PlayServicesUtils() {}

  /**
   * Check the device to make sure it has the Google Play Services APK. If
   * it doesn't, display a dialog that allows users to download the APK from
   * the Google Play Store or enable it in the device's system settings.
   */
  public static boolean isAvailable(@NonNull final Context context) {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (apiAvailability.isUserResolvableError(resultCode)) {
        // TODO: Figure out how to proceed - should we force users to install? What about emulators?
        // apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
      }
      Timber.d("This device does not have Google Play Services installed.");
      return false;
    }
    return true;
  }
}
