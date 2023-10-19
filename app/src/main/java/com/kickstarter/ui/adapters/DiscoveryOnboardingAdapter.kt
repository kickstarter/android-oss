package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryOnboardingViewBinding
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class DiscoveryOnboardingAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        DiscoveryOnboardingViewHolder.Delegate

    fun setShouldShowOnboardingView(shouldShowOnboardingView: Boolean) {
        clearSections()
        insertSection(SECTION_ONBOARDING_VIEW, emptyList<Boolean>())

        if (shouldShowOnboardingView) {
            setSection(SECTION_ONBOARDING_VIEW, listOf(true))
        } else {
            setSection(SECTION_ONBOARDING_VIEW, emptyList<Boolean>())
        }

        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.discovery_onboarding_view

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return DiscoveryOnboardingViewHolder(
            DiscoveryOnboardingViewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            delegate
        )
    }

    companion object {
        private const val SECTION_ONBOARDING_VIEW = 0
    }
}
