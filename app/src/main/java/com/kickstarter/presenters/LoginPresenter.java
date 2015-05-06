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
import com.kickstarter.models.CurrentUser;
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

    final Observable<Pair<String, String>> submit = login
      .withLatestFrom(emailAndPassword, (l, v) -> v);

    subscribeTo(submit, this::submit);
    subscribeTo(isValid, valid -> view().setFormEnabled(valid));
  }

  private static boolean isValid(final String email, final String password) {
    return StringUtils.isEmail(email) && password.length() > 0;
  }

  public void login() {
    login.onNext(null);
  }

  private void submit(final Pair<String, String> emailPassword) {
    client.login(emailPassword.first, emailPassword.second)
      .subscribe(this::success, this::error);
  }

  private void success(final AccessTokenEnvelope envelope) {
    if (hasView()) {
      CurrentUser.set(view().getApplicationContext(), envelope.user, envelope.access_token);
      Intent intent = new Intent(view(), DiscoveryActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      view().startActivity(intent);
    }
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
