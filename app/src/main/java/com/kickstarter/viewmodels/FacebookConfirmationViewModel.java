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
  private final PublishSubject<String> fbAccessToken = PublishSubject.create();
  private final PublishSubject<Boolean> sendNewsletters = PublishSubject.create();
  private final PublishSubject<Boolean> checkInitialNewsletterInput = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> signupSuccess = PublishSubject.create();
  public Observable<Void> signupSuccess() {
    return signupSuccess.asObservable();
  }
  private final BehaviorSubject<Boolean> checkInitialNewsletter = BehaviorSubject.create();
  public final Observable<Boolean> checkInitialNewsletter() {
    return checkInitialNewsletter;
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

  @Override
  public void createNewAccountClick() {
    createNewAccountClick.onNext(null);
  }

  @Override
  public void fbAccessToken(@NonNull final String s) {
    fbAccessToken.onNext(s);
  }

  @Override
  public void sendNewsletters(final boolean b) {
    sendNewsletters.onNext(b);
  }

  public FacebookConfirmationViewModel() {
    final Observable<Pair<String, Boolean>> tokenAndNewsletter = fbAccessToken
      .compose(Transformers.combineLatestPair(sendNewsletters));

    addSubscription(
      tokenAndNewsletter
        .compose(Transformers.takeWhen(createNewAccountClick))
        .flatMap(tn -> createNewAccount(tn.first, tn.second))
        .subscribe(this::registerWithFacebookSuccess)
    );
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(checkInitialNewsletterInput.subscribe(checkInitialNewsletter));
    checkInitialNewsletterInput.onNext(this.isInitialNewsletterChecked());

    addSubscription(signupError.subscribe(__ -> koala.trackRegisterError()));

    addSubscription(sendNewsletters.subscribe(koala::trackSignupNewsletterToggle));

    addSubscription(signupSuccess
        .subscribe(__ -> {
          koala.trackLoginSuccess();
          koala.trackRegisterSuccess();
        })
    );

    koala.trackFacebookConfirmation();

    koala.trackRegisterFormView();
  }

  public Observable<AccessTokenEnvelope> createNewAccount(@NonNull final String fbAccessToken, final boolean sendNewsletters) {
    return client.registerWithFacebook(fbAccessToken, sendNewsletters)
      .compose(Transformers.pipeApiErrorsTo(signupError));
  }

  private void registerWithFacebookSuccess(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    signupSuccess.onNext(null);
  }

  private boolean isInitialNewsletterChecked() {
    return currentConfig.getConfig().countryCode().equals("US");
  }
}
