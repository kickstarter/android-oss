package com.kickstarter.models

import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

interface ApolloEnvelope {
    fun pageInfoEnvelope(): PageInfoEnvelope?
}
