package com.kickstarter.services.firebase;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.ApiClientType;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

public class RegisterService extends IntentService {
  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUserType currentUser;

  public RegisterService() {
    super("RegisterService");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }

  @Override
  protected void onHandleIntent(final @Nullable Intent intent) {
    Timber.d("onHandleIntent");

    try {
      final String token = FirebaseInstanceId.getInstance().getToken();
      Timber.d("Token: %s", token);

      sendTokenToApi(token);
      subscribeToGlobalTopic(token);
    } catch (final Exception e) {
      Timber.e("Failed to complete token refresh: %s", e);
    }
  }

  /**
   * Persist token to app servers.
   *
   * @param token The new token.
   */
  private void sendTokenToApi(final @NonNull String token) {
    this.currentUser.observable()
      .take(1)
      .filter(ObjectUtils::isNotNull)
      .subscribe(__ ->
        this.apiClient.registerPushToken(token)
          .compose(Transformers.neverError())
          .toList().toBlocking().single()
      );
  }

  /**
   * Subscribe to generic global topic - not using more specific topics.
   *
   * @throws IOException if unable to reach the GCM PubSub service
   */
  private void subscribeToGlobalTopic(final @NonNull String token) throws IOException {
    FirebaseMessaging.getInstance().subscribeToTopic("global");
  }
}

