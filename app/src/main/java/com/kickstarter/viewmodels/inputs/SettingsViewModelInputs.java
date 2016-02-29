package com.kickstarter.viewmodels.inputs;

public interface SettingsViewModelInputs {
  void contactEmailClicked();
  void notifyMobileOfFollower(boolean checked);
  void notifyMobileOfFriendActivity(boolean checked);
  void notifyMobileOfUpdates(boolean checked);
  void notifyOfFollower(boolean checked);
  void notifyOfFriendActivity(boolean checked);
  void notifyOfUpdates(boolean checked);
  void sendHappeningNewsletter(boolean checked);
  void sendPromoNewsletter(boolean checked);
  void sendWeeklyNewsletter(boolean checked);

  /**
   * Call when the user taps the logout button
   */
  void logoutClicked();

  /**
   * Call when the user has confirmed that they want to log out.
   */
  void confirmLogoutClicked();

  /**
   * Call when the user dismiss the logout confirmation dialog.
   */
  void closeLogoutConfirmationClicked();
}
