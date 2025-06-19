package com.kickstarter.features.checkout.data

import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

data class AddOnsEnvelope(
    val addOnsList: List<Reward> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null,
)
