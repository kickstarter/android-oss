package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.presenters.inputs.FacebookConfirmationPresenterInputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.ui.activities.FacebookConfirmationActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class FacebookConfirmationPresenter extends Presenter<FacebookConfirmationActivity> implements
  FacebookConfirmationPresenterInputs {

  // INPUTS
  private final PublishSubject<Void> createNewAccountClick = PublishSubject.create();
  private final PublishSubject<String> fbAccessToken = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Boolean> sendNewsletters = PublishSubject.create();

  // OUTPUTS
  // registerSuccess
  // isFormSubmitting
  // isFormValid

  // ERRORS
  // registerError

  @Inject ApiClient client;

  public final FacebookConfirmationPresenterInputs inputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public Observable<AccessTokenEnvelope> createNewAccount(@NonNull final String fbAccessToken, final boolean sendNewsletters) {
    return client.registerWithFacebook(fbAccessToken, sendNewsletters);
    // add errors
  }

  @Override
  public void createNewAccountClick() {
    createNewAccountClick.onNext(null);
  }

  @Override
  public void fbAccessToken(@NonNull final String s) {
    fbAccessToken.onNext(s);
  }

  @Override
  public void loginClick() {
    loginClick.onNext(null);
  }

  @Override
  public void sendNewsletters(final boolean b) {
    sendNewsletters.onNext(b);
  }
}
