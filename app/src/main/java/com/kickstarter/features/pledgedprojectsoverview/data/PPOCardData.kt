package com.kickstarter.features.pledgedprojectsoverview.data

import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType

data class PPOCardData(
    val ppoCardViewType: PPOCardViewType,
    val projectName : String,
    val creatorName : String,
    val pledgeAmount : Int,
)