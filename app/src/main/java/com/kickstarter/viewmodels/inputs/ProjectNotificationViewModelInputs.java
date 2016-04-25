package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.ProjectNotification;

public interface ProjectNotificationViewModelInputs {
  /**
   * Call when the enable switch is clicked.
   */
  void enabledSwitchClick(boolean enabled);

  /**
   * Call when a notification is bound to the viewholder.
   */
  void projectNotification(ProjectNotification projectNotification);
}
