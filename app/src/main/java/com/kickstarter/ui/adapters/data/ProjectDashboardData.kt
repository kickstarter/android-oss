package com.kickstarter.ui.adapters.data

import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope

data class ProjectDashboardData(val project: Project, val projectStatsEnvelope: ProjectStatsEnvelope, val isViewingSingleProject: Boolean)
