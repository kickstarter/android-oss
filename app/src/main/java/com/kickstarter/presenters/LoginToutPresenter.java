package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Presenter;
import com.kickstarter.ui.activities.LoginToutActivity;

public class LoginToutPresenter extends Presenter<LoginToutActivity> {
  private boolean forward = false;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
  }

  public void takeForward(final boolean forward) {
    this.forward = forward;
  }

  public void takeLoginButtonClick() {
    view().startLoginActivity(forward);
  }
}
