package com.kickstarter.presenters;

import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.libs.StringUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginActivity;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import timber.log.Timber;

public class LoginPresenter extends Presenter<LoginActivity> {
  private static final KickstarterClient client = new KickstarterClient();

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
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(isValid -> view().login_button.setEnabled(isValid));

    subscriptions.add(subscription);
  }

  public static boolean isValid(CharSequence email, CharSequence password) {
    return StringUtils.isEmail(email)  && password.length() > 0;
  }

  public void login(String email_address, String password) {
    Subscription subscription = client.login(email_address, password)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(envelope -> {
        User.setCurrent(envelope.user);
        Intent intent = new Intent(view(), DiscoveryActivity.class);
        view().startActivity(intent);
      });

    subscriptions.add(subscription);
  }
}
