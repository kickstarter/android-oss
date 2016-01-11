package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.RefTag;
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

  /**
   * Call with the un-parceled ref tag from the activity. Pass `null` if there is no parceled ref tag.
   */
  void intentRefTag(final @Nullable RefTag refTag);
}
