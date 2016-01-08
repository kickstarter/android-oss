package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

public interface ProjectViewModelInputs {
  void initializer(final @NonNull Project project);
  void backProjectClicked();
  void shareClicked();
  void blurbClicked();
  void commentsClicked();
  void creatorNameClicked();
  void managePledgeClicked();
  void updatesClicked();
  void playVideoClicked();
  void viewPledgeClicked();
  void starClicked();
  void rewardClicked(final @NonNull Reward reward);
  void loginSuccess();
}
