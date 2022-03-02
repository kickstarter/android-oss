package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.models.Update
import com.kickstarter.models.Update.Companion.builder

object UpdateFactory {
    @JvmStatic
    fun update(): Update {
        val creator = creator().toBuilder().id(278438049L).build()
        val project = project().toBuilder().creator(creator).build()
        val updatesUrl = "https://www.kck.str/projects/" + project.creator()
            .param() + "/" + project.param() + "/posts"

        val web = Update.Urls.Web.builder()
            .update(updatesUrl + "id")
            .likes("$updatesUrl/likes")
            .build()

        return builder()
            .body("Update body")
            .id(1234)
            .isPublic(true)
            .projectId(5678)
            .sequence(11111)
            .title("First update")
            .urls(Update.Urls.builder().web(web).build())
            .build()
    }

    fun backersOnlyUpdate(): Update {
        return update()
            .toBuilder()
            .isPublic(false)
            .build()
    }
}
