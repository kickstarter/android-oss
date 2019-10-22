package com.kickstarter.services.mutations

import com.kickstarter.models.Backing
import com.kickstarter.models.Reward

data class UpdateBacking(val backing: Backing, val amount: String, val locationId: String?, val reward: Reward?, val paymentSourceId: String?)
