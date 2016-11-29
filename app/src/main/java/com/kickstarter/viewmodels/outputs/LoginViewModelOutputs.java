package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import rx.Observable;

public interface LoginViewModelOutputs {
  /**
   * Finish the activity with a successful result.
   */
  Observable<Void> loginSuccess();

  /**
   * Fill the view's email address and show a dialog indicating the user's password has been reset.
   */
  Observable<String> prefillEmailFromPasswordReset();

  /**
   * Emits an email string and boolean to determine whether or not to display the reset password success dialog.
   */
  Observable<Pair<Boolean, String>> showResetPasswordSuccessDialog();

  /**
   * Emits a boolean to determine whether or not the login button should be enabled.
   */
  Observable<Boolean> setLoginButtonIsEnabled();
}
