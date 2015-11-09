package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Koala;
import com.kickstarter.libs.Presenter;
import com.kickstarter.ui.activities.LoginToutActivity;

import javax.inject.Inject;

import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> {
  @Inject Koala koala;

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
