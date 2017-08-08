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
import com.kickstarter.viewmodels.errors.FacebookConfirmationViewModelErrors;
import com.kickstarter.viewmodels.inputs.FacebookConfirmationViewModelInputs;
import com.kickstarter.viewmodels.outputs.FacebookConfirmationViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.pipeApiErrorsTo;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public class FacebookConfirmationViewModel extends ActivityViewModel<FacebookConfirmationActivity> implements
  FacebookConfirmationViewModelInputs, FacebookConfirmationViewModelOutputs, FacebookConfirmationViewModelErrors {
  private final ApiClientType client;
  private final CurrentUserType currentUser;
  private final CurrentConfigType currentConfig;

  // INPUTS
  private final PublishSubject<Void> createNewAccountClick = PublishSubject.create();
  public void createNewAccountClick() {
    this.createNewAccountClick.onNext(null);
  }
  private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();
  public void sendNewslettersClick(final boolean b) {
    this.sendNewslettersClick.onNext(b);
  }

  // OUTPUTS
  private final BehaviorSubject<String> prefillEmail = BehaviorSubject.create();
  public @NonNull Observable<String> prefillEmail() {
    return this.prefillEmail;
  }

  private final PublishSubject<Void> signupSuccess = PublishSubject.create();
  public @NonNull Observable<Void> signupSuccess() {
    return this.signupSuccess;
  }
  private final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();
  public @NonNull Observable<Boolean> sendNewslettersIsChecked() {
    return this.sendNewslettersIsChecked;
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
  public Observable<String> signupError() {
    return this.signupError
      .takeUntil(this.signupSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  public final FacebookConfirmationViewModelInputs inputs = this;
  public final FacebookConfirmationViewModelOutputs outputs = this;
  public final FacebookConfirmationViewModelErrors errors = this;

  public FacebookConfirmationViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentConfig = environment.currentConfig();
    this.currentUser = environment.currentUser();

    final Observable<String> facebookAccessToken = intent()
      .map(i -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN))
      .ofType(String.class);

    final Observable<Pair<String, Boolean>> tokenAndNewsletter = facebookAccessToken
      .compose(combineLatestPair(sendNewslettersIsChecked));

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

  public Observable<AccessTokenEnvelope> createNewAccount(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
    return this.client.registerWithFacebook(fbAccessToken, sendNewsletters)
      .compose(pipeApiErrorsTo(this.signupError))
      .compose(neverError());
  }

  private void registerWithFacebookSuccess(final @NonNull AccessTokenEnvelope envelope) {
    this.currentUser.login(envelope.user(), envelope.accessToken());
    this.signupSuccess.onNext(null);
  }
}
