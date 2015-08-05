package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiErrorHandler;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.libs.StringUtils;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.ApiError;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginActivity;
import com.kickstarter.ui.activities.TwoFactorActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.subjects.PublishSubject;

public class LoginPresenter extends Presenter<LoginActivity> {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;
  private final PublishSubject<Void> login = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);

    final Observable<OnTextChangeEvent> email = viewSubject
      .flatMap(v -> WidgetObservable.text(v.email));

    final Observable<OnTextChangeEvent> password = viewSubject
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
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success, this::error);
  }

  private void success(final AccessTokenEnvelope envelope) {
    if (hasView()) {
      currentUser.set(envelope.user, envelope.access_token);
      final Intent intent = new Intent(view(), DiscoveryActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      view().startActivity(intent);
    }
  }

  private void error(final Throwable e) {
    if (!hasView()) {
      return;
    }

    new ApiErrorHandler(e, view()) {
      @Override
      public void handleApiError(final ApiError api_error) {
        switch (api_error.errorEnvelope().ksrCode()) {
          case TFA_REQUIRED:
          case TFA_FAILED:
            startTwoFactorActivity();
            break;
          case INVALID_XAUTH_LOGIN:
            displayError(R.string.Login_does_not_match_any_of_our_records);
            break;
          default:
            displayError(R.string.Unable_to_login);
            break;
        }

      }
    }.handleError();
  }

  private void startTwoFactorActivity() {
    final Intent intent = new Intent(view(), TwoFactorActivity.class);
    // TODO: Fetching the details from the view seems a little dirty, it would be nice if we
    // could pass along the email and password that generated the event.
    intent.putExtra("email", view().email.getText().toString());
    intent.putExtra("password", view().password.getText().toString());
    view().startActivity(intent);
  }
}
