package com.kickstarter.services.mutations

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Reward

data class CreateBackingData(val project: Project, val amount: String, val paymentSourceId: String? = null, val setupIntentClientSecret: String? = null, val locationId: String?, val reward: Reward? = null, val rewardsIds: List<Reward>? = null, val refTag: RefTag?, val stripeCardId: String? = null, val incremental: Boolean? = null)
