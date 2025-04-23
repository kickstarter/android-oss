package com.kickstarter.ui.viewholders

import androidx.compose.ui.Modifier
import com.kickstarter.databinding.ActivityRewardShippedViewBinding
import com.kickstarter.features.rewardtracking.RewardTrackingActivityFeed
import com.kickstarter.models.Activity
import com.kickstarter.ui.compose.designsystem.KSTheme

class RewardShippedViewHolder(
    private val binding: ActivityRewardShippedViewBinding,
    private val delegate: Delegate?
) : ActivityListViewHolder(binding.root) {

    interface Delegate {
        fun projectClicked(viewHolder: RewardShippedViewHolder?, activity: Activity?)
        fun trackingNumberClicked(url: String)
    }

    override fun onBind() {
        binding.rewardShippedComposeView.setContent {
            KSTheme {
                RewardTrackingActivityFeed(
                    modifier = Modifier,
                    trackingNumber = activity().trackingNumber() ?: "",
                    projectName = activity().project()?.name() ?: "",
                    photo = activity().project()?.photo(),
                    projectClicked = { projectOnClick() },
                    trackShipmentClicked = { trackingNumberClicked() }
                )
        }
        }
    }

    private fun projectOnClick() {
        delegate?.projectClicked(this, activity())
    }

    private fun trackingNumberClicked() {
        delegate?.trackingNumberClicked(activity().trackingUrl() ?: "")
    }
}
