package com.kickstarter.mock.factories

import com.kickstarter.models.CreatorDetails

class CreatorDetailsFactory private constructor() {
    companion object {
        fun creatorDetails(): CreatorDetails {
            return CreatorDetails.builder()
                    .backingsCount(3)
                    .launchedProjectsCount(2)
                    .name("Creator Name")
                    .build()
        }
    }
}
