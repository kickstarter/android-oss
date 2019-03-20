package com.kickstarter.ui.data

import com.kickstarter.models.Project
import com.kickstarter.models.Reward

data class PledgeData(val rewardScreenLocation: ScreenLocation, val reward: Reward, val project: Project)