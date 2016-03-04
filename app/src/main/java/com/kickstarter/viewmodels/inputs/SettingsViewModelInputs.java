package com.kickstarter.viewmodels.inputs;

public interface SettingsViewModelInputs {
  void contactEmailClicked();
  void notifyMobileOfFollower(boolean checked);
  void notifyMobileOfFriendActivity(boolean checked);
  void notifyMobileOfUpdates(boolean checked);
  void notifyOfFollower(boolean checked);
  void notifyOfFriendActivity(boolean checked);
  void notifyOfUpdates(boolean checked);

  /**
   * Call when the user toggles the Kickstarter Loves Games newsletter switch.
   */
  void sendGamesNewsletter(boolean checked);

  /**
   * Call when the user toggles the Happening newsletter switch.
   */
  void sendHappeningNewsletter(boolean checked);

  /**
   * Call when the user toggles the Kickstarter News & Events newsletter switch.
   */
  void sendPromoNewsletter(boolean checked);

  /**
   * Call when the user toggles the Projects We Love newsletter switch.
   */
  void sendWeeklyNewsletter(boolean checked);

  /**
   * Call when the user taps the logout button.
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
