package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendBackingViewBinding
import com.kickstarter.databinding.ActivitySampleFriendFollowViewBinding
import com.kickstarter.databinding.ActivitySampleProjectViewBinding
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.models.Activity
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class DiscoveryActivitySampleAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        ActivitySampleFriendFollowViewHolder.Delegate,
        ActivitySampleFriendBackingViewHolder.Delegate,
        ActivitySampleProjectViewHolder.Delegate

    fun takeActivity(activity: Activity?) {
        clearSections()
        insertSection(SECTION_ACTIVITY_SAMPLE_VIEW, emptyList<Activity>())
        activity?.let {
            setSection(SECTION_ACTIVITY_SAMPLE_VIEW, listOf(activity))
        } ?: setSection(SECTION_ACTIVITY_SAMPLE_VIEW, emptyList<Activity>())

        submitList(items())
    }

    @LayoutRes
    override fun layout(sectionRow: SectionRow?): Int {
        return if (sectionRow != null && objectFromSectionRow(sectionRow) is Activity) {
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
        } else {
            R.layout.empty_view
        }
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
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
        private const val SECTION_ACTIVITY_SAMPLE_VIEW = 0
    }
}
