package com.kickstarter.viewmodels.inputs;

import android.support.annotation.Nullable;

import com.kickstarter.services.apiresponses.PushNotificationEnvelope;

public interface WebViewViewModelInputs {

  /**
   * Call when a push notification envelope is unparceled from the intent.
   */
  void takePushNotificationEnvelope(final @Nullable PushNotificationEnvelope envelope);
}
