package com.kickstarter.services.firebase;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kickstarter.R;

import timber.log.Timber;

public class UnregisterService extends IntentService {
  public UnregisterService() {
    super("UnregisterService");
  }

  @Override
  protected void onHandleIntent(final @NonNull Intent intent) {
    Timber.d("onHandleIntent");

    try {
      final String token = FirebaseInstanceId.getInstance().getToken();
      // idk if this is right
      FirebaseInstanceId.getInstance().deleteToken(getString(R.string.gcm_defaultSenderId), FirebaseMessaging.INSTANCE_ID_SCOPE);
      Timber.d("Deleted token");
    } catch (final Exception e) {
      Timber.e("Failed to delete token: %s", e);
    }
  }
}

