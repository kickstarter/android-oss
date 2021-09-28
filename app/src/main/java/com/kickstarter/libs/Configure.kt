package com.kickstarter.libs

import com.kickstarter.ui.data.ProjectData

interface Configure {
    fun configureWith(projectData: ProjectData)
}
