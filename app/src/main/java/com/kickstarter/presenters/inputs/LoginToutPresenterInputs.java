package com.kickstarter.presenters.inputs;

import android.content.Intent;

import com.facebook.CallbackManager;
import com.kickstarter.ui.activities.LoginToutActivity;

import java.util.List;

public interface LoginToutPresenterInputs {
  void facebookLoginClick(LoginToutActivity activity, List<String> facebookPermissions);
  void facebookCallbackManager(CallbackManager callbackManager);
  void activityResult(int requestCode, int resultCode, Intent intent);
  void reason(String __);
}
