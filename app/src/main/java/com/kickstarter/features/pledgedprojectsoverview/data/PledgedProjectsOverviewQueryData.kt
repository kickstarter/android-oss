package com.kickstarter.features.pledgedprojectsoverview.data

data class PledgedProjectsOverviewQueryData(
    val first: Int? = null,
    val after: String? = null,
    val last: Int? = null,
    val before: String? = null
)
