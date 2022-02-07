package com.kickstarter.ui.adapters.data

import android.util.Pair
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams

class ThanksData(
    val backedProject: Project,
    val category: Category,
    val recommendedProjects: List<Pair<Project, DiscoveryParams>>
)
