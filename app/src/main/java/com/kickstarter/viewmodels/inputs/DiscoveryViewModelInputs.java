package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

public interface DiscoveryViewModelInputs {
  void initializer(DiscoveryParams __);
  void nextPage();
  void projectClick(Project __);
}
