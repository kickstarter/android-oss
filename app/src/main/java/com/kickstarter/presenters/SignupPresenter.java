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
import com.kickstarter.presenters.inputs.SignupPresenterInputs;
import com.kickstarter.presenters.outputs.SignupPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.ui.activities.SignupActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class SignupPresenter extends Presenter<SignupActivity> implements SignupPresenterInputs, SignupPresenterOutputs {

  final private class SignupData {
    @NonNull final String fullName;
    @NonNull final String email;
    @NonNull final String password;

    public SignupData(@NonNull final String fullName, @NonNull final String email, @NonNull final String password) {
      this.fullName = fullName;
      this.email = email;
      this.password = password;
    }

    public boolean isValid() {
      return fullName.length() > 0 && StringUtils.isEmail(email) && password.length() > 0;
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
  public final Observable<Void> signupSuccess() { return signupSuccessSubject.asObservable(); }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public SignupPresenterInputs inputs() { return this; }
  public SignupPresenterOutputs outputs() { return this; }

  @Override
  public void fullName(@NonNull final String s) {
    fullName.onNext(s);
  }

  @Override
  public void email(@NonNull final String s) { email.onNext(s); }
  @Override
  public void password(@NonNull final String s) { password.onNext(s); }
  @Override
  public void sendNewsletter(@NonNull final Boolean b) { sendNewsletter.onNext(b); }
  @Override
  public void signupClick(@NonNull final View v) { signupClick.onNext(v); }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<SignupData> signupData = Observable.combineLatest(fullName, email, password, SignupData::new);

    addSubscription(
      RxUtils.takePairWhen(viewSubject, signupData)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vd -> {
          vd.first.setFormEnabled(vd.second.isValid());
        })
    );
  }

  private void submit(@NonNull final String fullName, @NonNull final String email, @NonNull final String password) {
    client.signup(fullName, email, password, password, 1) // TODO: get newsletter value
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success); // TODO: errors
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    signupSuccessSubject.onNext(null);
  }
}
