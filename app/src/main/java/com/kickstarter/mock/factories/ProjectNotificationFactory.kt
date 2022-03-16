package com.kickstarter.mock.factories
import com.kickstarter.models.ProjectNotification

object ProjectNotificationFactory {
    @JvmStatic
    fun disabled(): ProjectNotification {
        return enabled().toBuilder()
            .email(false)
            .mobile(false)
            .build()
    }

    @JvmStatic
    fun enabled(): ProjectNotification {
        return ProjectNotification.builder()
            .id(IdFactory.id().toLong())
            .email(true)
            .mobile(true)
            .project(project())
            .urls(urls())
            .build()
    }

    private fun project(): ProjectNotification.Project {
        return ProjectNotification.Project.builder().id(1L)
            .name("SKULL GRAPHIC TEE").build()
    }

    private fun urls(): ProjectNotification.Urls {
        val api = ProjectNotification.Urls.Api.builder().notification("/url").build()
        return ProjectNotification.Urls.builder().api(api).build()
    }
}
