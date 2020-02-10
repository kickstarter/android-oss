package com.kickstarter.mock.factories

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectTracking

class ProjectTrackingFactory private constructor() {
    companion object {
        fun project(project: Project): ProjectTracking {
            return ProjectTracking.builder()
                    .project(project)
                    .build()
        }

        fun project(project: Project, intentRefTag: RefTag?, cookieRefTag: RefTag?): ProjectTracking {
            return ProjectTracking.builder()
                    .project(project)
                    .refTagFromCookie(intentRefTag)
                    .refTagFromIntent(cookieRefTag)
                    .build()
        }
    }
}
