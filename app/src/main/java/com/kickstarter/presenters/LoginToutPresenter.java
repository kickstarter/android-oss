package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.Presenter;
import com.kickstarter.ui.activities.LoginActivity;
import com.kickstarter.ui.activities.LoginToutActivity;

import timber.log.Timber;

public class LoginToutPresenter extends Presenter<LoginToutActivity> {
  private boolean forward = false;

  @Override
  protected void onCreate(final Context context, Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
  }

  public void takeForward(final boolean forward) {
    this.forward = forward;
  }

  public void takeLoginButtonClick() {
    view().startLoginActivity(forward);
  }
}
