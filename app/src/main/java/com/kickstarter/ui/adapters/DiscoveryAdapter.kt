package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendBackingViewBinding
import com.kickstarter.databinding.ActivitySampleFriendFollowViewBinding
import com.kickstarter.databinding.ActivitySampleProjectViewBinding
import com.kickstarter.databinding.DiscoveryOnboardingViewBinding
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemLightsOnBinding
import com.kickstarter.databinding.ProjectCardViewBinding
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder
import com.kickstarter.ui.viewholders.EditorialViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.ProjectCardViewHolder

class DiscoveryAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        DiscoveryOnboardingViewHolder.Delegate,
        EditorialViewHolder.Delegate,
        ActivitySampleFriendFollowViewHolder.Delegate,
        ActivitySampleFriendBackingViewHolder.Delegate,
        ActivitySampleProjectViewHolder.Delegate,
        ProjectCardViewHolder.Delegate

    fun takeActivity(activity: Activity?) {
        if (activity == null) {
            setSection(SECTION_ACTIVITY_SAMPLE_VIEW, emptyList<Any>())
        } else {
            setSection(SECTION_ACTIVITY_SAMPLE_VIEW, listOf(activity))
        }

        submitList(items())
    }

    fun setShouldShowEditorial(editorial: Editorial?) {
        if (editorial == null) {
            setSection(SECTION_EDITORIAL_VIEW, emptyList<Any>())
        } else {
            setSection(SECTION_EDITORIAL_VIEW, listOf(editorial))
        }
        submitList(items())
    }

    fun setShouldShowOnboardingView(shouldShowOnboardingView: Boolean) {
        if (shouldShowOnboardingView) {
            setSection(SECTION_ONBOARDING_VIEW, listOf<Any?>(null))
        } else {
            setSection(SECTION_ONBOARDING_VIEW, emptyList<Any>())
        }
        submitList(items())
    }

    fun takeProjects(projects: List<Pair<Project, DiscoveryParams>>) {
        setSection(SECTION_PROJECT_CARD_VIEW, projects)
        submitList(items())
    }

    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return when {
            sectionRow.section() == SECTION_ONBOARDING_VIEW ->
                R.layout.discovery_onboarding_view

            sectionRow.section() == SECTION_EDITORIAL_VIEW ->
                R.layout.item_lights_on

            sectionRow.section() == SECTION_ACTIVITY_SAMPLE_VIEW -> {
                if (objectFromSectionRow(sectionRow) is Activity) {
                    val activity = objectFromSectionRow(sectionRow) as Activity
                    return when {
                        activity.category() == Activity.CATEGORY_BACKING -> {
                            R.layout.activity_sample_friend_backing_view
                        }
                        activity.category() == Activity.CATEGORY_FOLLOW -> {
                            R.layout.activity_sample_friend_follow_view
                        }
                        else -> {
                            R.layout.activity_sample_project_view
                        }
                    }
                }
                R.layout.empty_view
            }
            else ->
                R.layout.project_card_view
        }
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.discovery_onboarding_view -> DiscoveryOnboardingViewHolder(
                DiscoveryOnboardingViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.item_lights_on -> EditorialViewHolder(
                ItemLightsOnBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.project_card_view -> ProjectCardViewHolder(
                ProjectCardViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.activity_sample_friend_backing_view -> ActivitySampleFriendBackingViewHolder(
                ActivitySampleFriendBackingViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.activity_sample_friend_follow_view -> ActivitySampleFriendFollowViewHolder(
                ActivitySampleFriendFollowViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.activity_sample_project_view -> ActivitySampleProjectViewHolder(
                ActivitySampleProjectViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            else -> EmptyViewHolder(
                EmptyViewBinding.inflate(
                    LayoutInflater.from(
                        viewGroup.context
                    ),
                    viewGroup, false
                )
            )
        }
    }

    companion object {
        private const val SECTION_ONBOARDING_VIEW = 0
        private const val SECTION_EDITORIAL_VIEW = 1
        private const val SECTION_ACTIVITY_SAMPLE_VIEW = 2
        private const val SECTION_PROJECT_CARD_VIEW = 3
    }

    init {
        insertSection(SECTION_ONBOARDING_VIEW, emptyList<Any>())
        insertSection(SECTION_EDITORIAL_VIEW, emptyList<Any>())
        insertSection(SECTION_ACTIVITY_SAMPLE_VIEW, emptyList<Any>())
        insertSection(SECTION_PROJECT_CARD_VIEW, emptyList<Any>())
    }
}
