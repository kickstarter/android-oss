package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Presenter;
import com.kickstarter.ui.activities.LoginToutActivity;

import javax.inject.Inject;

import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> {
  private final PublishSubject<String> showLoginWithIntent = PublishSubject.create();
  public void showLoginWithIntent(@Nullable final String intent) {
    showLoginWithIntent.onNext(intent);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

//    addSubscription(showLoginWithIntent
//      .subscribe(koala::trackLoginRegisterTout)
//    );
  }
}
