package com.kickstarter.libs.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.services.ApiClient;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

public class RegisterService extends IntentService {
  private static final String WORKER_THREAD_NAME = "RegisterService";
  private static final String[] TOPICS = {"global"};

  @Inject protected ApiClient apiClient;

  public RegisterService() {
    super(WORKER_THREAD_NAME);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }

  @Override
  protected void onHandleIntent(@NonNull final Intent intent) {
    Timber.d("onHandleIntent");

    try {
      // This initially hits the network to retrieve the token, subsequent calls are local
      final InstanceID instanceID = InstanceID.getInstance(this);

      // R.string.gcm_defaultSenderId is derived from google-services.json
      final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
      Timber.d("Token: " + token);

      sendTokenToAppServers(token);
      subscribeTopics(token);
    } catch (final Exception e) {
      Timber.e("Failed to complete token refresh: " +  e.toString());
    }
  }

  /**
   * Persist token to app servers.
   *
   * @param token The new token.
   */
  private void sendTokenToAppServers(@NonNull final String token) {
    //apiClient.registerPushToken(token);
  }

  /**
   * Subscribe to any topics of interest, as defined by the TOPICS constant.
   *
   * @throws IOException if unable to reach the GCM PubSub service
   */
  private void subscribeTopics(@NonNull final String token) throws IOException {
    final GcmPubSub pubSub = GcmPubSub.getInstance(this);
    for (final String topic : TOPICS) {
      pubSub.subscribe(token, "/topics/" + topic, null);
    }
  }
}

