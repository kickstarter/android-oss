package com.kickstarter.services.gcm;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Enables various aspects of handling messages such as detecting different downstream message types,
 * determining upstream send status, and automatically displaying simple notifications on the app’s behalf.
 */
public class MessageService extends GcmListenerService {
  @Inject Gson gson;
  @Inject PushNotifications pushNotifications;

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }

  /**
   * Called when message is received.
   *
   * @param from SenderID of the sender.
   * @param data Data bundle containing message data as key/value pairs.
   *             For Set of keys use data.keySet().
   */
  @Override
  public void onMessageReceived(@NonNull final String from, @NonNull final Bundle data) {
    final String senderId = getString(R.string.gcm_defaultSenderId);
    if (!from.equals(senderId)) {
      Timber.e("Received a message from " + from + ", expecting " + senderId);
      return;
    }

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(gson.fromJson(data.getString("activity"), Activity.class))
      .gcm(gson.fromJson(data.getString("gcm"), GCM.class))
      .build();

    if (envelope == null) {
      Timber.e("Cannot parse message, malformed or unexpected data: " + data.toString());
      return;
    }

    pushNotifications.show(envelope);
  }
}
