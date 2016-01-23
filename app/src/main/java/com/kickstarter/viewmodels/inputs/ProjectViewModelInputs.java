package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;

public interface ProjectViewModelInputs {
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
