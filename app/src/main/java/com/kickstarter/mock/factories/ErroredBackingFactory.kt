package com.kickstarter.mock.factories

import com.kickstarter.models.ErroredBacking
import org.joda.time.DateTime

class ErroredBackingFactory private constructor() {
    companion object {
        fun erroredBacking(): ErroredBacking {
            return ErroredBacking.builder()
                    .project(ErroredBacking.Project.builder()
                            .finalCollectionDate(DateTime.parse("2020-04-02T18:08:32Z"))
                            .name("Some Project Name")
                            .slug("slug")
                            .build())
                    .build()
        }
    }
}
