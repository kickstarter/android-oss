package com.kickstarter.services.mutations

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Reward

data class CreateCheckoutData(val project: Project, val amount: String, val locationId: String?, val rewardsIds: List<Reward> = listOf(), val refTag: RefTag?)
