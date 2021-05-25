package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

class PageInfoEnvelopeFactory {

    companion object {
        fun pageInfoEnvelope(): PageInfoEnvelope {
            return PageInfoEnvelope.builder()
                .endCursor("WzMyNDk1MzMzXQ==")
                .startCursor("WzMyNDk1MzMzXQ==")
                .build()
        }
    }
}
