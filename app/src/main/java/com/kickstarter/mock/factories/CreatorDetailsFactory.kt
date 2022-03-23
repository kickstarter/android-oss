package com.kickstarter.mock.factories

import com.kickstarter.models.CreatorDetails

object CreatorDetailsFactory {
    @JvmStatic
    fun creatorDetails(): CreatorDetails {
        return CreatorDetails.builder()
            .backingsCount(3)
            .launchedProjectsCount(2)
            .build()
    }
}
