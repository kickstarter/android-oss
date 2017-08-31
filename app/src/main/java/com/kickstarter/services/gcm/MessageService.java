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

public class MessageService extends GcmListenerService {
  @Inject protected Gson gson;
  @Inject protected PushNotifications pushNotifications;

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }

  /**
   * Called when a message is received from GCM.
   *
   * @param from SenderID of the sender.
   * @param data Data bundle containing message data as key/value pairs.
   */
  @Override
  public void onMessageReceived(final @NonNull String from, final @NonNull Bundle data) {
    final String senderId = getString(R.string.gcm_defaultSenderId);
    if (!from.equals(senderId)) {
      Timber.e("Received a message from %s, expecting %s", from, senderId);
      return;
    }

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(this.gson.fromJson(data.getString("activity"), Activity.class))
      .gcm(this.gson.fromJson(data.getString("gcm"), GCM.class))
      .message(this.gson.fromJson(data.getString("message"), PushNotificationEnvelope.Message.class))
      .project(this.gson.fromJson(data.getString("project"), PushNotificationEnvelope.Project.class))
      .survey(this.gson.fromJson(data.getString("survey"), PushNotificationEnvelope.Survey.class))
      .build();

    if (envelope == null) {
      Timber.e("Cannot parse message, malformed or unexpected data: %s", data.toString());
      return;
    }

    Timber.d("Received message: %s", envelope.toString());
    this.pushNotifications.add(envelope);
  }
}
