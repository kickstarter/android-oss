package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.InternalBuildEnvelope

object InternalBuildEnvelopeFactory {
    fun internalBuildEnvelope(): InternalBuildEnvelope {
        return InternalBuildEnvelope.builder()
            .build(123456)
            .changelog("Bug fixes")
            .newerBuildAvailable(false)
            .build()
    }

    @JvmStatic
    fun newerBuildAvailable(): InternalBuildEnvelope {
        return internalBuildEnvelope().toBuilder()
            .newerBuildAvailable(true)
            .build()
    }
}
