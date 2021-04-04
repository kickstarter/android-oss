package com.kickstarter.services.mutations

import com.kickstarter.models.Backing
import com.kickstarter.models.Reward

data class UpdateBackingData(
    val backing: Backing,
    val amount: String? = null,
    val locationId: String? = null,
    val rewardsIds: List<Reward>? = null,
    val paymentSourceId: String? = null
)
