package com.kickstarter.models.mutations

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Reward

data class CreateBacking(val project: Project, val amount: String, val paymentSourceId: String, val locationId: String?, val reward: Reward?, val refTag: RefTag?)