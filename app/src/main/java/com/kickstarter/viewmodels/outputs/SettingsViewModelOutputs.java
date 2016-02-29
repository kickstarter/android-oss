package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.User;
import com.kickstarter.ui.data.Newsletter;

import rx.Observable;

public interface SettingsViewModelOutputs {
  Observable<Newsletter> sendNewsletterConfirmation();
  Observable<Void> updateSuccess();
  Observable<User> user();

  /**
   * Emits a boolean that determines if the logout confirmation should be displayed.
   */
  Observable<Boolean> showConfirmLogoutPrompt();

  /**
   * Emits when its time to log the user out.
   */
  Observable<Void> logout();
}
