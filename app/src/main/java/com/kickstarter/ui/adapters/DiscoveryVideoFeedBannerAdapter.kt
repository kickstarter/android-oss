package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryVideoFeedBannerViewBinding
import com.kickstarter.ui.viewholders.DiscoveryVideoFeedBannerViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class DiscoveryVideoFeedBannerAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        DiscoveryVideoFeedBannerViewHolder.Delegate

    fun setShouldShowBanner(shouldShow: Boolean) {
        clearSections()
        insertSection(SECTION_BANNER, emptyList<Boolean>())

        if (shouldShow) {
            setSection(SECTION_BANNER, listOf(true))
        } else {
            setSection(SECTION_BANNER, emptyList<Boolean>())
        }

        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.discovery_video_feed_banner_view

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return DiscoveryVideoFeedBannerViewHolder(
            DiscoveryVideoFeedBannerViewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            delegate
        )
    }

    companion object {
        private const val SECTION_BANNER = 0
    }
}
