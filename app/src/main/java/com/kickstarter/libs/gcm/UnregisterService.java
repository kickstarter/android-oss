package com.kickstarter.libs.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kickstarter.R;

import timber.log.Timber;

public class UnregisterService extends IntentService {
  private static final String WORKER_THREAD_NAME = "UnregisterService";

  public UnregisterService() {
    super(WORKER_THREAD_NAME);
  }

  @Override
  protected void onHandleIntent(@NonNull final Intent intent) {
    Timber.d("onHandleIntent");

    try {
      final InstanceID instanceID = InstanceID.getInstance(this);
      instanceID.deleteToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE);
      Timber.d("Deleted token");
    } catch (final Exception e) {
      Timber.e("Failed to delete token", e);
    }
  }
}

