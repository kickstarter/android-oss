package com.kickstarter.features.search.data

import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

data class SearchEnvelope(
    val projectList: List<Project> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null
)
