package com.kickstarter.services.firebase;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;

import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class MessageService extends FirebaseMessagingService {
  @Inject protected Gson gson;
  @Inject protected PushNotifications pushNotifications;

  @Override
  public void onNewToken(@NonNull final String s) {
    super.onNewToken(s);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }

  /**
   * Called when a message is received from Firebase.
   *
   * @param remoteMessage Object containing message information.
   */
  @Override
  public void onMessageReceived(final RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);

    final String senderId = getString(R.string.gcm_defaultSenderId);
    final String from = remoteMessage.getFrom();
    if (!TextUtils.equals(from, senderId)) {
      Timber.e("Received a message from %s, expecting %s", from, senderId);
      return;
    }

    final Map<String, String> data = remoteMessage.getData();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(this.gson.fromJson(data.get("activity"), Activity.class))
      .erroredPledge(this.gson.fromJson(data.get("errored_pledge"), PushNotificationEnvelope.ErroredPledge.class))
      .gcm(this.gson.fromJson(data.get("gcm"), GCM.class))
      .message(this.gson.fromJson(data.get("message"), PushNotificationEnvelope.Message.class))
      .project(this.gson.fromJson(data.get("project"), PushNotificationEnvelope.Project.class))
      .survey(this.gson.fromJson(data.get("survey"), PushNotificationEnvelope.Survey.class))
      .build();

    if (envelope == null) {
      Timber.e("Cannot parse message, malformed or unexpected data: %s", data.toString());
      return;
    }

    Timber.d("Received message: %s", envelope.toString());
    this.pushNotifications.add(envelope);
  }
}
