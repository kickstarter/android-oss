package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ApiErrorHandler;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.CurrentUser;
import com.kickstarter.services.ApiError;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.TwoFactorActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.subjects.PublishSubject;

public class TwoFactorPresenter extends Presenter<TwoFactorActivity> {
  @Inject CurrentUser currentUser;
  @Inject KickstarterClient client;
  private final PublishSubject<Void> login = PublishSubject.create();
  private final PublishSubject<Void> resend = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeEmailAndPassword(final String email, final String password) {
    final Observable<Pair<String, String>> emailAndPassword = Observable.just(Pair.create(email, password));

    final Observable<String> code = viewSubject
      .filter(v -> v != null)
      .flatMap(v -> WidgetObservable.text(v.code))
      .map(v -> v.text().toString());

    final Observable<Boolean> isValid = code
      .map(c -> TwoFactorPresenter.isValid(c));

    final Observable<LoginCredentials> submit = login
      .withLatestFrom(code, (s, c) -> c)
      .withLatestFrom(emailAndPassword, (c, ep) -> new LoginCredentials(ep.first, ep.second, c));

    final Observable<Pair<String, String>> r = resend
      .withLatestFrom(emailAndPassword, (v, ep) -> ep);


    subscribeTo(r, this::resendSubmit);
    subscribeTo(submit, this::submit);
    subscribeTo(isValid, valid -> view().setLoginEnabled(valid));
  }

  private static boolean isValid(final String code) {
    return code.length() > 0;
  }

  public void resend() {
    resend.onNext(null);
  }

  public void login() {
    login.onNext(null);
  }

  private void success(final AccessTokenEnvelope envelope) {
    if (hasView()) {
      currentUser.set(envelope.user, envelope.access_token);
      Intent intent = new Intent(view(), DiscoveryActivity.class)
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
          case TFA_FAILED:
            displayError(R.string.The_code_provided_does_not_match);
            break;
          default:
            displayError(R.string.Unable_to_login);
            break;
        }
      }
    }.handleError();
  }

  private void submit(final LoginCredentials loginCredentials) {
    Observable<AccessTokenEnvelope> e = client.login(loginCredentials.email(), loginCredentials.password(), loginCredentials.code())
      .observeOn(AndroidSchedulers.mainThread());
    subscribeTo(e, this::success, this::error);
  }

  private void resendSubmit(final Pair<String, String> emailAndPassword) {
    Observable<AccessTokenEnvelope> envelope = client.login(emailAndPassword.first, emailAndPassword.second)
      .observeOn(AndroidSchedulers.mainThread());
    // TODO: We could notify on connection error
    subscribeTo(envelope, (final AccessTokenEnvelope e) -> {}, (final Throwable error) -> {});
  }

  private class LoginCredentials {
    private final String email;
    private final String password;
    private final String code;

    public LoginCredentials(final String email, final String password, final String code) {
      this.email = email;
      this.password = password;
      this.code = code;
    }

    public String email() {
      return email;
    }

    public String password() {
      return password;
    }

    public String code() {
      return code;
    }
  }
}
