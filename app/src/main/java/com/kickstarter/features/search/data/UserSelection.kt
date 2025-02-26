package com.kickstarter.features.search.data

import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.type.ProjectState

data class SearchEnvelope(
    val projectList: List<Project>,
    val pageInfo: PageInfoEnvelope
)

data class UserSelection(
    val searchTerm: String?,
    val sort: DiscoveryParams.Sort?,
    val projectStatus: ProjectState?, // Maybe PublicProjectState for non-logged in users
    val categoryId: Long?,
    //val raised: RaisedBuckets?,
//    goal: GoalBuckets?,
//    pledged: PledgedBuckets?,
//    locationId: Long?
)
