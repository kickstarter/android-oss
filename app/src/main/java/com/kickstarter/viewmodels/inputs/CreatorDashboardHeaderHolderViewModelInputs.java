package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;


public interface CreatorDashboardHeaderHolderViewModelInputs {
  void projectAndStats(Project project, ProjectStats projectStats);
}
