package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.Project;

public interface DiscoveryViewModelInputs {
  void projectClicked(Project project);
  void nextPage();
  void filterButtonClicked();
}
