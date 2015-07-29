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
  private boolean forwardFlag = false;

  @Override
  protected void onCreate(final Context context, Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
  }

  public void takeForwardFlag(final boolean forwardFlag) {
    this.forwardFlag = forwardFlag;
  }

  public void takeLoginButtonClick() {
    final Intent intent = new Intent(view(), LoginActivity.class);
    if (forwardFlag) {
      Timber.d("Starting login activity with forward flag");
      intent.putExtra("forward", true);
      view().startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    } else {
      view().startActivity(intent);
    }
    view().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
