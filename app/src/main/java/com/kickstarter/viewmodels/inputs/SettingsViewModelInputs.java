package com.kickstarter.viewmodels.inputs;

public interface SettingsViewModelInputs {
  void contactEmailClicked();
  void notifyMobileOfFollower(boolean checked);
  void notifyMobileOfFriendActivity(boolean checked);
  void notifyMobileOfUpdates(boolean checked);
  void notifyOfFollower(boolean checked);
  void notifyOfFriendActivity(boolean checked);
  void notifyOfUpdates(boolean checked);
  void sendHappeningNewsletter(boolean checked, String name);
  void sendPromoNewsletter(boolean checked, String name);
  void sendWeeklyNewsletter(boolean checked, String name);

  /**
   * Call when the user taps the logout button
   */
  void logoutClicked();

  /**
   * Call when the user dismiss the logout confirmation dialog.
   */
  void closeLogoutConfirmationClicked();
}
