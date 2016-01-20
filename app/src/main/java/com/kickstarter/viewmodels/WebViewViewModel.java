package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.activities.WebViewActivity;
import com.kickstarter.viewmodels.inputs.WebViewViewModelInputs;

import javax.inject.Inject;

import rx.subjects.PublishSubject;

public final class WebViewViewModel extends ViewModel<WebViewActivity> implements WebViewViewModelInputs {
  protected @Inject Koala koala;

  final PublishSubject<PushNotificationEnvelope> pushNotificationEnvelope = PublishSubject.create();
  public void takePushNotificationEnvelope(final @Nullable PushNotificationEnvelope envelope) {
    pushNotificationEnvelope.onNext(envelope);
  }

  public final WebViewViewModelInputs inputs = this;

  @Override
  protected void onCreate(@NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(
      pushNotificationEnvelope
        .filter(ObjectUtils::isNotNull)
        .take(1)
        .subscribe(koala::trackPushNotification)
    );
  }
}
