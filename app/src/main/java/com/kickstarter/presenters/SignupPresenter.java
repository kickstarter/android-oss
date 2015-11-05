package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.presenters.errors.SignupPresenterErrors;
import com.kickstarter.presenters.inputs.SignupPresenterInputs;
import com.kickstarter.presenters.outputs.SignupPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.ApiError;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.SignupActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class SignupPresenter extends Presenter<SignupActivity> implements SignupPresenterInputs, SignupPresenterOutputs,
SignupPresenterErrors {

  final private class SignupData {
    @NonNull final String fullName;
    @NonNull final String email;
    @NonNull final String password;
    @NonNull final Boolean sendNewsletter;

    public SignupData(@NonNull final String fullName, @NonNull final String email, @NonNull final String password,
      @NonNull final Boolean sendNewsletter) {
      this.fullName = fullName;
      this.email = email;
      this.password = password;
      this.sendNewsletter = sendNewsletter;
    }

    public boolean isValid() {
      return fullName.length() > 0 && StringUtils.isEmail(email) && password.length() >= 6;
    }
  }

  // INPUTS
  private final PublishSubject<String> fullName = PublishSubject.create();
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();
  private final PublishSubject<Boolean> sendNewsletter = PublishSubject.create();
  private final PublishSubject<View> signupClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> signupSuccessSubject = PublishSubject.create();
  public final Observable<Void> signupSuccess() {
    return signupSuccessSubject.asObservable();
  }

  //ERRRORS
  private final PublishSubject<List<String>> signupErrorSubject = PublishSubject.create();
  public final Observable<List<String>> signupError() {
    return signupErrorSubject.asObservable();
  }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public SignupPresenterInputs inputs() { return this; }
  public SignupPresenterOutputs outputs() { return this; }
  public SignupPresenterErrors errors() { return this; }

  @Override
  public void fullName(@NonNull final String s) {
    fullName.onNext(s);
  }

  @Override
  public void email(@NonNull final String s) {
    email.onNext(s);
  }

  @Override
  public void password(@NonNull final String s) {
    password.onNext(s);
  }
  @Override
  public void sendNewsletter(@NonNull final Boolean b) {
    sendNewsletter.onNext(b);
  }
  @Override
  public void signupClick(@NonNull final View v) {
    signupClick.onNext(v);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<SignupData> signupData = Observable.combineLatest(fullName, email, password, sendNewsletter, SignupData::new);

    addSubscription(
      RxUtils.takePairWhen(viewSubject, signupData)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vd -> {
          vd.first.setFormEnabled(vd.second.isValid());
        })
    );

    addSubscription(
      RxUtils.takeWhen(signupData, signupClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(d -> {
            submit(d.fullName, d.email, d.password, d.sendNewsletter);
          }
        )
    );
  }

  private void submit(@NonNull final String fullName, @NonNull final String email, @NonNull final String password,
    @NonNull final Boolean sendNewsletters) {
    client.signup(fullName, email, password, password, sendNewsletters ? 1 : 0)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success, this::error);
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    signupSuccessSubject.onNext(null);
  }

  private void error(@NonNull final Throwable e) {
    if (e instanceof ApiError) {
      final ApiError error = (ApiError) e;
      final ErrorEnvelope envelope = error.errorEnvelope();
      final List<String> errorMessages = envelope.errorMessages();
      signupErrorSubject.onNext(errorMessages);
    }
  }
}
