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
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.WebViewActivity;
import com.kickstarter.viewmodels.outputs.WebViewViewModelOutputs;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class WebViewViewModel extends ViewModel<WebViewActivity> implements WebViewViewModelOutputs {
  protected @Inject Koala koala;

  final PublishSubject<PushNotificationEnvelope> pushNotificationEnvelope = PublishSubject.create();

  final BehaviorSubject<String> toolbarTitle = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> toolbarTitle() {
    return toolbarTitle;
  }

  final BehaviorSubject<String> url = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> url() {
    return url;
  }

  public final WebViewViewModelOutputs outputs = this;

  @Override
  protected void onCreate(@NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(
      intent
        .map(i -> i.getStringExtra(IntentKey.TOOLBAR_TITLE))
        .ofType(String.class)
        .subscribe(toolbarTitle::onNext)
    );

    addSubscription(
      intent
        .map(i -> i.getStringExtra(IntentKey.URL))
        .ofType(String.class)
        .subscribe(url::onNext)
    );

    addSubscription(
      intent
        .map(i -> i.getParcelableExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE))
        .ofType(PushNotificationEnvelope.class)
        .subscribe(pushNotificationEnvelope::onNext)
    );

    pushNotificationEnvelope
      .filter(ObjectUtils::isNotNull)
      .take(1)
      .subscribe(koala::trackPushNotification);
  }
}
