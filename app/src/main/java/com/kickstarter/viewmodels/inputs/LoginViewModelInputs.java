package com.kickstarter.viewmodels.inputs;

public interface LoginViewModelInputs {
  /**
   * Call when the email address field is changed.
   */
  void email(String __);

  /**
   * Call when the login button is clicked.
   */
  void loginClick();

  /**
   * Call when the password field is changed.
   */
  void password(String __);

  /**
   * Call when the user cancels or dismisses the reset password success confirmation dialog.
   */
  void resetPasswordConfirmationDialogDismissed();
}
