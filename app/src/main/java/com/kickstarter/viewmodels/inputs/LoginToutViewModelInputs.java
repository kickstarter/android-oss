package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.ui.activities.LoginToutActivity;

import java.util.List;

public interface LoginToutViewModelInputs {
  /**
   * Call when the Login to Facebook button is clicked.
   */
  void facebookLoginClick(final @NonNull LoginToutActivity activity, final @NonNull List<String> facebookPermissions);

  /**
   * Call when the login button is clicked.
   */
  void loginClick();

  /**
   * Call when the signup button is clicked.
   */
  void signupClick();
}
