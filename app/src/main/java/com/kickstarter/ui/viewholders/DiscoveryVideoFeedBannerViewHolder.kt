package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.DiscoveryVideoFeedBannerViewBinding
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSVideoFeedBanner

class DiscoveryVideoFeedBannerViewHolder(
    private val binding: DiscoveryVideoFeedBannerViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun discoveryVideoFeedBannerViewHolderClick(viewHolder: DiscoveryVideoFeedBannerViewHolder?)
    }

    init {
        binding.videoFeedBannerComposeView.setContent {
            KSTheme {
                KSVideoFeedBanner(
                    onButtonClick = { delegate.discoveryVideoFeedBannerViewHolderClick(this) }
                )
            }
        }
    }

    override fun bindData(data: Any?) {
    }
}
