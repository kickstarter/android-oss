package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.login.LoginResult;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.ui.activities.LoginToutActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> {
  @Inject CurrentUser currentUser;

  // INPUTS
  private final PublishSubject<Void> loginClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  public final Observable<Void> loginSuccess() {
    return loginSuccess.asObservable();
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void login(@NonNull final LoginResult result) {

  }
}
