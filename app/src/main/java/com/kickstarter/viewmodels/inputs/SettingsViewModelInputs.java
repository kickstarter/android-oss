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
   * Changes the user's subscription state to the Kickstarter Loves Games newsletter.
   * @param checked `true` to subscribe, `false` to unsubscribe.
   */
  void sendGamesNewsletter(boolean checked);

  /**
   * Changes the user's subscription state to the Happening newsletter.
   * @param checked `true` to subscribe, `false` to unsubscribe.
   */
  void sendHappeningNewsletter(boolean checked);

  /**
   * Changes the user's subscription state to the Kickstarter News & Events newsletter.
   * @param checked `true` to subscribe, `false` to unsubscribe.
   */
  void sendPromoNewsletter(boolean checked);

  /**
   * Changes the user's subscription state to the Kickstarter Loves Games newsletter.
   * @param checked `true` to subscribe, `false` to unsubscribe.
   */
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
