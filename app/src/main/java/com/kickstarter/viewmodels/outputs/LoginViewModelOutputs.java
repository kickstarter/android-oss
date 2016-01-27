package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface LoginViewModelOutputs {
  /**
   * Fill the view's email address and show a dialog indicating the user's password has been rest.
   */
  Observable<String> prefillEmailFromPasswordReset();

  /**
   * Finish the activity with a successful result.
   */
  Observable<Void> loginSuccess();
}
