package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.I18nUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.FacebookConfirmationActivity;
import com.kickstarter.viewmodels.errors.FacebookConfirmationViewModelErrors;
import com.kickstarter.viewmodels.inputs.FacebookConfirmationViewModelInputs;
import com.kickstarter.viewmodels.outputs.FacebookConfirmationViewModelOutputs;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class FacebookConfirmationViewModel extends ViewModel<FacebookConfirmationActivity> implements
  FacebookConfirmationViewModelInputs, FacebookConfirmationViewModelOutputs, FacebookConfirmationViewModelErrors {
  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;
  protected @Inject CurrentConfig currentConfig;

  // INPUTS
  private final PublishSubject<Void> createNewAccountClick = PublishSubject.create();
  public void createNewAccountClick() {
    createNewAccountClick.onNext(null);
  }
  private final PublishSubject<String> fbAccessToken = PublishSubject.create();
  public void fbAccessToken(final @NonNull String s) {
    fbAccessToken.onNext(s);
  }
  private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();
  public void sendNewslettersClick(final boolean b) {
    sendNewslettersClick.onNext(b);
  }

  // OUTPUTS
  private final PublishSubject<Void> signupSuccess = PublishSubject.create();
  public Observable<Void> signupSuccess() {
    return signupSuccess.asObservable();
  }
  final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();
  public final Observable<Boolean> sendNewslettersIsChecked() {
    return sendNewslettersIsChecked;
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
  public Observable<String> signupError() {
    return signupError
      .takeUntil(signupSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  public final FacebookConfirmationViewModelInputs inputs = this;
  public final FacebookConfirmationViewModelOutputs outputs = this;
  public final FacebookConfirmationViewModelErrors errors = this;

  public FacebookConfirmationViewModel() {
    final Observable<Pair<String, Boolean>> tokenAndNewsletter = fbAccessToken
      .compose(Transformers.combineLatestPair(sendNewslettersIsChecked));

    tokenAndNewsletter
      .compose(Transformers.takeWhen(createNewAccountClick))
      .flatMap(tn -> createNewAccount(tn.first, tn.second))
      .compose(bindToLifecycle())
      .subscribe(this::registerWithFacebookSuccess);

    sendNewslettersClick
      .compose(bindToLifecycle())
      .subscribe(sendNewslettersIsChecked::onNext);
  }

  @Override
  protected void onCreate(final @NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    currentConfig.observable()
      .take(1)
      .map(config -> I18nUtils.isCountryUS(config.countryCode()))
      .subscribe(sendNewslettersIsChecked::onNext);

    signupError
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackRegisterError());

    sendNewslettersClick
      .compose(bindToLifecycle())
      .subscribe(koala::trackSignupNewsletterToggle);

    signupSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        koala.trackLoginSuccess();
        koala.trackRegisterSuccess();
      });

    koala.trackFacebookConfirmation();
    koala.trackRegisterFormView();
  }

  public Observable<AccessTokenEnvelope> createNewAccount(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
    return client.registerWithFacebook(fbAccessToken, sendNewsletters)
      .compose(Transformers.pipeApiErrorsTo(signupError))
      .compose(Transformers.neverError());
  }

  private void registerWithFacebookSuccess(final @NonNull AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    signupSuccess.onNext(null);
  }
}
