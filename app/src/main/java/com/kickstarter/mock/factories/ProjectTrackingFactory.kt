package com.kickstarter.mock.factories

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectTracking

class ProjectTrackingFactory private constructor() {
    companion object {
        fun project(project: Project): ProjectTracking {
            return ProjectTracking.builder()
                    .project(ProjectFactory.project())
                    .refTagFromCookie(RefTag.discovery())
                    .refTagFromIntent(RefTag.discovery())
                    .build()
        }
    }
}
