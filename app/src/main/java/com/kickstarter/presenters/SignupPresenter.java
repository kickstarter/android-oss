package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.presenters.inputs.SignupPresenterInputs;
import com.kickstarter.presenters.outputs.SignupPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.SignupActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class SignupPresenter extends Presenter<SignupActivity> implements SignupPresenterInputs, SignupPresenterOutputs {

  // INPUTS
  private final PublishSubject<String> name = PublishSubject.create();
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();
  private final PublishSubject<Boolean> newsletterSwitch = PublishSubject.create();
  private final PublishSubject<View> signupClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> signupSuccessSubject = PublishSubject.create();
  public final Observable<Void> signupSuccess() { return signupSuccessSubject.asObservable(); }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public SignupPresenterInputs inputs() { return this; }
  public SignupPresenterOutputs outputs() { return this; }

  @Override
  public void name(@NonNull final String s) { name.onNext(s); }
  @Override
  public void email(@NonNull final String s) { email.onNext(s); }
  @Override
  public void password(@NonNull final String s) { password.onNext(s); }
  @Override
  public void newsletterSwitch(@NonNull final Boolean b) { newsletterSwitch.onNext(b); }
  @Override
  public void signupClick(@NonNull final View v) { signupClick.onNext(v); }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

  }
}
