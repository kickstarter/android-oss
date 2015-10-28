package com.kickstarter.libs.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import timber.log.Timber;

/**
 * Handle the creation, rotation, and updating of registration tokens.
 */
public class TokenListenerService extends InstanceIDListenerService {
  /**
   * Called if InstanceID token is updated. This may occur if the security of
   * the previous token had been compromised. This call is initiated by the
   * InstanceID provider.
   */
  @Override
  public void onTokenRefresh() {
    // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
    Timber.d("Token refreshed, creating new RegistrationService intent");
    final Intent intent = new Intent(this, RegistrationService.class);
    startService(intent);
  }
}
