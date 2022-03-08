package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.ProjectFactory.failedProject
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectFactory.successfulProject
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.models.Activity
import com.kickstarter.models.Activity.Companion.builder
import org.joda.time.DateTime

object ActivityFactory {
    @JvmStatic
    fun activity(): Activity {
        return builder()
            .category(Activity.CATEGORY_WATCH)
            .createdAt(DateTime(123))
            .id(IdFactory.id().toLong())
            .updatedAt(DateTime(456))
            .project(project())
            .user(user())
            .build()
    }

    @JvmStatic
    fun friendBackingActivity(): Activity {
        return activity().toBuilder()
            .category(Activity.CATEGORY_BACKING)
            .build()
    }

    @JvmStatic
    fun projectStateChangedActivity(): Activity {
        return activity().toBuilder()
            .category(Activity.CATEGORY_FAILURE)
            .project(failedProject())
            .build()
    }

    @JvmStatic
    fun projectStateChangedPositiveActivity(): Activity {
        return activity().toBuilder()
            .category(Activity.CATEGORY_SUCCESS)
            .project(successfulProject())
            .build()
    }

    @JvmStatic
    fun updateActivity(): Activity {
        return activity().toBuilder()
            .category(Activity.CATEGORY_UPDATE)
            .project(project())
            .user(user())
            .build()
    }
}
