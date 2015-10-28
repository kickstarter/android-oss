package com.kickstarter.libs.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kickstarter.R;

import java.io.IOException;

import timber.log.Timber;

public class RegistrationService extends IntentService {
  public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
  public static final String REGISTRATION_COMPLETE = "registrationComplete";

  private static final String WORKER_THREAD_NAME = "RegistrationService";
  private static final String[] TOPICS = {"global"};

  public RegistrationService() {
    super(WORKER_THREAD_NAME);
  }

  @Override
  protected void onHandleIntent(@NonNull final Intent intent) {
    Timber.d("onHandleIntent called");
    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    try {
      // Initially this call goes out to the network to retrieve the token, subsequent calls
      // are local.
      final InstanceID instanceID = InstanceID.getInstance(this);
      // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
      // See https://developers.google.com/cloud-messaging/android/start for details on this file.
      final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
      Timber.d("GCM Registration Token: " + token);

      // TODO: Implement this method to send any registration to your app's servers.
      sendRegistrationToServer(token);

      // Subscribe to topic channels
      subscribeTopics(token);

      // You should store a boolean that indicates whether the generated token has been
      // sent to your server. If the boolean is false, send the token to your server,
      // otherwise your server should have already received the token.
      sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
    } catch (Exception e) {
      Timber.d("Failed to complete token refresh", e);
      // If an exception happens while fetching the new token or updating our registration data
      // on a third-party server, this ensures that we'll attempt the update at a later time.
      sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
    }
    // Notify UI that registration has completed, so the progress indicator can be hidden.
    final Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
    LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
  }

  /**
   * Persist registration to third-party servers.
   *
   * Modify this method to associate the user's GCM registration token with any server-side account
   * maintained by your application.
   *
   * @param token The new token.
   */
  private void sendRegistrationToServer(@NonNull final String token) {
    // Add custom implementation, as needed.
  }

  /**
   * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
   *
   * @param token GCM token
   * @throws IOException if unable to reach the GCM PubSub service
   */
  private void subscribeTopics(@NonNull final String token) throws IOException {
    GcmPubSub pubSub = GcmPubSub.getInstance(this);
    for (final String topic : TOPICS) {
      pubSub.subscribe(token, "/topics/" + topic, null);
    }
  }
}

