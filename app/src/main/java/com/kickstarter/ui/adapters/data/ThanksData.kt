package com.kickstarter.ui.adapters.data

import com.kickstarter.models.Category
import com.kickstarter.models.Project

class ThanksData(
    val backedProject: Project,
    val category: Category,
    val recommendedProjects: List<Project>
)
