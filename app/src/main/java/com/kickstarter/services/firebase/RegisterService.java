package com.kickstarter.services.firebase;

import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.DeviceRegistrar;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.ApiClientType;

import javax.inject.Inject;

import timber.log.Timber;

public class RegisterService extends JobService {
  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUserType currentUser;
  public static final String REGISTER_SERVICE = "Register-service";

  @Override
  public void onCreate() {
    super.onCreate();
    ((KSApplication) getApplicationContext()).component().inject(this);
  }


  @Override
  public boolean onStartJob(final JobParameters job) {
    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
      final String newToken = instanceIdResult.getToken();
      Timber.d("newToken", newToken);
      sendTokenToApi(newToken);
      subscribeToGlobalTopic();
    });

    return true;
  }

  @Override
  public boolean onStopJob(final JobParameters job) {
    return false;
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
   */
  private void subscribeToGlobalTopic() {
    FirebaseMessaging.getInstance().subscribeToTopic(DeviceRegistrar.TOPIC_GLOBAL);
  }
}

