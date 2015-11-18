package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.presenters.errors.FacebookConfirmationPresenterErrors;
import com.kickstarter.presenters.inputs.FacebookConfirmationPresenterInputs;
import com.kickstarter.presenters.outputs.FacebookConfirmationPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.FacebookConfirmationActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class FacebookConfirmationPresenter extends Presenter<FacebookConfirmationActivity> implements
  FacebookConfirmationPresenterInputs, FacebookConfirmationPresenterOutputs, FacebookConfirmationPresenterErrors {

  // INPUTS
  private final PublishSubject<Void> createNewAccountClick = PublishSubject.create();
  private final PublishSubject<String> fbAccessToken = PublishSubject.create();
  private final PublishSubject<Boolean> sendNewsletters = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> signupSuccess = PublishSubject.create();
  public Observable<Void> signupSuccess() {
    return signupSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
  public Observable<String> signupError() {
    return signupError
      .takeUntil(signupSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public final FacebookConfirmationPresenterInputs inputs = this;
  public final FacebookConfirmationPresenterOutputs outputs = this;
  public final FacebookConfirmationPresenterErrors errors = this;

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

  public FacebookConfirmationPresenter() {
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
}
