package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.CurrentUser;
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

    subscribeTo(submit, this::submit);
    subscribeTo(isValid, valid -> view().setLoginEnabled(valid));
  }

  private static boolean isValid(final String code) {
    return code.length() > 0;
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
  }

  private void submit(final LoginCredentials loginCredentials) {
    client.login(loginCredentials.email(), loginCredentials.password(), loginCredentials.code())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success, this::error);
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
