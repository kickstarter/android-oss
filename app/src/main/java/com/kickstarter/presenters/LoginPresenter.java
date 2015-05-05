package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.libs.StringUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginActivity;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;

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

  public static boolean isValid(final CharSequence email, final CharSequence password) {
    return StringUtils.isEmail(email)  && password.length() > 0;
  }

  public void login(final String email_address, final String password) {
    Subscription subscription = client.login(email_address, password)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new Subscriber<AccessTokenEnvelope>() {
        @Override
        public void onNext(AccessTokenEnvelope envelope) {
          User.setCurrent(envelope.user);
          Intent intent = new Intent(view(), DiscoveryActivity.class);
          view().startActivity(intent);
        }

        @Override
        public void onError(final Throwable e) {
          if (hasView()) {
            Context context = view().getApplicationContext();

            // TODO: Check error, e.g. is it a connection timeout?
            Toast toast = Toast.makeText(context,
              context.getResources().getString(R.string.Login_does_not_match_any_of_our_records),
              Toast.LENGTH_LONG);
            toast.show();
          }
        }

        @Override
        public void onCompleted() {}
      });

    subscriptions.add(subscription);
  }
}
