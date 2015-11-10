package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.presenters.inputs.LoginToutPresenterInputs;
import com.kickstarter.ui.activities.LoginToutActivity;

import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> implements LoginToutPresenterInputs {

  // INPUTS
  private final PublishSubject<String> reason = PublishSubject.create();
  public LoginToutPresenterInputs inputs = this;
  @Override
  public void reason(@Nullable final String r) {
    reason.onNext(r);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(reason
      .subscribe(koala::trackLoginRegisterTout)
    );
  }
}
