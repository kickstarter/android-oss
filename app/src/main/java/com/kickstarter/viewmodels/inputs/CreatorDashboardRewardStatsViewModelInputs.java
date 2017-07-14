package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

public interface CreatorDashboardRewardStatsViewModelInputs {
  void projectAndStats(Project project, ProjectStatsEnvelope projectStatsEnvelope);
}
