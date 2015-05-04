package com.kickstarter.presenters;

import android.os.Bundle;
import android.util.Patterns;

import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.ui.activities.LoginActivity;

import rx.Observable;
import rx.Subscription;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import timber.log.Timber;

public class LoginPresenter extends Presenter<LoginActivity> {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Observable<OnTextChangeEvent> email = viewSubject
      .filter(v -> v != null)
      .flatMap(v -> WidgetObservable.text(v.email_address));

    Observable<OnTextChangeEvent> password = viewSubject
      .filter(v -> v != null)
      .flatMap(v -> WidgetObservable.text(v.password));

    Subscription subscription = RxUtils.combineLatestPair(email, password)
      .map(pair -> Patterns.EMAIL_ADDRESS.matcher(pair.first.text()).matches() && pair.second.text().length() > 0)
      .filter(r -> r == true)
      .subscribe(o -> Timber.d("valid"));

    subscriptions.add(subscription);
  }
}
