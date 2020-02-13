package com.kickstarter.mock.factories

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData

class ProjectDataFactory private constructor() {
    companion object {
        fun project(project: Project): ProjectData {
            return ProjectData.builder()
                    .project(project)
                    .build()
        }

        fun project(project: Project, intentRefTag: RefTag?, cookieRefTag: RefTag?): ProjectData {
            return ProjectData.builder()
                    .project(project)
                    .refTagFromIntent(intentRefTag)
                    .refTagFromCookie(cookieRefTag)
                    .build()
        }
    }
}
