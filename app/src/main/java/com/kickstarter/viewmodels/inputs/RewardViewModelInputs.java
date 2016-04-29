package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

public interface RewardViewModelInputs {
  /**
   * Call with a reward and project when data is bound to the view.
   */
  void projectAndReward(Project project, Reward reward);

  /**
   * Call when the user clicks on a reward.
   */
  void rewardClicked();
}
