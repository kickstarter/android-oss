package com.kickstarter.viewmodels.inputs;

import android.support.v7.widget.SwitchCompat;

public interface SettingsViewModelInputs {
  void contactEmailClicked();
  void notifyMobileOfFollower(boolean __);
  void notifyMobileOfFriendActivity(boolean __);
  void notifyMobileOfUpdates(boolean __);
  void notifyOfFollower(boolean __);
  void notifyOfFriendActivity(boolean __);
  void notifyOfUpdates(boolean __);
  void sendHappeningNewsletter(SwitchCompat __);
  void sendPromoNewsletter(SwitchCompat __);
  void sendWeeklyNewsletter(SwitchCompat __);
}
