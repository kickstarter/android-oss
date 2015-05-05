package com.kickstarter.presenters;

import android.os.Bundle;

import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.libs.StringUtils;
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
      .map(p -> LoginPresenter.isValid(p.first.text(), p.second.text()))
      .subscribe(isValid -> Timber.d("valid: " + isValid.toString()));

    subscriptions.add(subscription);
  }

  public static boolean isValid(CharSequence email, CharSequence password) {
    return StringUtils.isEmail(email)  && password.length() > 0;
  }
}
