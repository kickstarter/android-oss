package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.models.Reward;

public interface ProjectViewModelInputs {
  /**
   * Call when the star button has been clicked.
   */
  void starClicked();

  /**
   * Call when the user has successfully logged in.
   */
  void loginSuccess();

  void backProjectClicked();
  void shareClicked();
  void blurbClicked();
  void commentsClicked();
  void creatorNameClicked();
  void managePledgeClicked();
  void updatesClicked();
  void playVideoClicked();
  void viewPledgeClicked();
  void rewardClicked(final @NonNull Reward reward);
}
