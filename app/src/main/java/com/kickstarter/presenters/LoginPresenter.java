package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
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
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.subjects.PublishSubject;

public class LoginPresenter extends Presenter<LoginActivity> {
  private static final KickstarterClient client = new KickstarterClient();
  private final PublishSubject<Void> login = PublishSubject.create();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Observable<OnTextChangeEvent> email = viewSubject
      .filter(v -> v != null)
      .flatMap(v -> WidgetObservable.text(v.email_address));

    final Observable<OnTextChangeEvent> password = viewSubject
      .filter(v -> v != null)
      .flatMap(v -> WidgetObservable.text(v.password));

    final Observable<Pair<String, String>> emailAndPassword =
      RxUtils.combineLatestPair(email, password)
        .map(v -> Pair.create(v.first.text().toString(), v.second.text().toString()));

    final Observable<Boolean> isValid = emailAndPassword
      .map(v -> LoginPresenter.isValid(v.first, v.second));

    final Observable<AccessTokenEnvelope> accessToken = login
      .withLatestFrom(emailAndPassword, (l, v) -> v)
      .flatMap(v -> client.login(v.first, v.second));

    subscribeTo(accessToken, this::success, this::error);

    subscribeTo(isValid, valid -> view().login_button.setEnabled(valid));
  }

  private static boolean isValid(final CharSequence email, final CharSequence password) {
    return StringUtils.isEmail(email) && password.length() > 0;
  }

  public void login() {
    login.onNext(null);
  }

  private void success(final AccessTokenEnvelope envelope) {

    User.setCurrent(envelope.user);
    Intent intent = new Intent(view(), DiscoveryActivity.class);
    view().startActivity(intent);
  }

  private void error(final Throwable e) {
    if (hasView()) {
      Context context = view().getApplicationContext();

      // TODO: Check error, e.g. is it a connection timeout?
      Toast toast = Toast.makeText(context,
        context.getResources().getString(R.string.Login_does_not_match_any_of_our_records),
        Toast.LENGTH_LONG);
      toast.show();
    }
  }
}
