package com.kickstarter.presenters.inputs;

import com.kickstarter.models.Project;

public interface DiscoveryPresenterInputs {
  void projectClick(Project project);
  void nextPage();
}
