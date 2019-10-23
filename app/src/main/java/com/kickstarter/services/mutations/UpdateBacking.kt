package com.kickstarter.services.mutations

import com.kickstarter.models.Backing
import com.kickstarter.models.Reward

data class UpdateBacking(val backing: Backing,
                         val amount: String? = null,
                         val locationId: String? = null,
                         val reward: Reward? = null,
                         val paymentSourceId: String? = null)
