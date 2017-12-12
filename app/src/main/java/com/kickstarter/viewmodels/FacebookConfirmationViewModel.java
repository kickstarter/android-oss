package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.I18nUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.FacebookConfirmationActivity;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.pipeApiErrorsTo;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface FacebookConfirmationViewModel {

  interface Inputs {
    void createNewAccountClick();
    void sendNewslettersClick(boolean __);
  }

  interface Outputs {
    Observable<String> prefillEmail();
    Observable<String> signupError();
    Observable<Void> signupSuccess();
    Observable<Boolean> sendNewslettersIsChecked();
  }

  final class ViewModel extends ActivityViewModel<FacebookConfirmationActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;
    private final CurrentConfigType currentConfig;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentConfig = environment.currentConfig();
      this.currentUser = environment.currentUser();

      final Observable<String> facebookAccessToken = intent()
        .map(i -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN))
        .ofType(String.class);

      final Observable<Pair<String, Boolean>> tokenAndNewsletter = facebookAccessToken
        .compose(combineLatestPair(this.sendNewslettersIsChecked));

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.FACEBOOK_USER))
        .ofType(ErrorEnvelope.FacebookUser.class)
        .map(ErrorEnvelope.FacebookUser::email)
        .compose(bindToLifecycle())
        .subscribe(this.prefillEmail::onNext);

      tokenAndNewsletter
        .compose(takeWhen(this.createNewAccountClick))
        .flatMap(tn -> createNewAccount(tn.first, tn.second))
        .compose(bindToLifecycle())
        .subscribe(this::registerWithFacebookSuccess);

      this.sendNewslettersClick
        .compose(bindToLifecycle())
        .subscribe(this.sendNewslettersIsChecked::onNext);
    }

    @Override
    protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
      super.onCreate(context, savedInstanceState);

      this.currentConfig.observable()
        .take(1)
        .map(config -> I18nUtils.isCountryUS(config.countryCode()))
        .subscribe(this.sendNewslettersIsChecked::onNext);

      this.signupError
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackRegisterError());

      this.sendNewslettersClick
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackSignupNewsletterToggle);

      this.signupSuccess
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.koala.trackLoginSuccess();
          this.koala.trackRegisterSuccess();
        });

      this.koala.trackFacebookConfirmation();
      this.koala.trackRegisterFormView();
    }

    private Observable<AccessTokenEnvelope> createNewAccount(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
      return this.client.registerWithFacebook(fbAccessToken, sendNewsletters)
        .compose(pipeApiErrorsTo(this.signupError))
        .compose(neverError());
    }

    private void registerWithFacebookSuccess(final @NonNull AccessTokenEnvelope envelope) {
      this.currentUser.login(envelope.user(), envelope.accessToken());
      this.signupSuccess.onNext(null);
    }

    private final PublishSubject<Void> createNewAccountClick = PublishSubject.create();
    private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();

    private final BehaviorSubject<String> prefillEmail = BehaviorSubject.create();
    private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
    private final PublishSubject<Void> signupSuccess = PublishSubject.create();
    private final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void createNewAccountClick() {
      this.createNewAccountClick.onNext(null);
    }
    @Override public void sendNewslettersClick(final boolean b) {
      this.sendNewslettersClick.onNext(b);
    }

    @Override public @NonNull Observable<String> prefillEmail() {
      return this.prefillEmail;
    }
    @Override public @NonNull Observable<String> signupError() {
      return this.signupError
        .takeUntil(this.signupSuccess)
        .map(ErrorEnvelope::errorMessage);
    }
    @Override public @NonNull Observable<Void> signupSuccess() {
      return this.signupSuccess;
    }
    @Override public @NonNull Observable<Boolean> sendNewslettersIsChecked() {
      return this.sendNewslettersIsChecked;
    }
  }
}
