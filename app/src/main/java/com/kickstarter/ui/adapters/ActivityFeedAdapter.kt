package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.*
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.Activity
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.viewholders.*

class ActivityFeedAdapter(private val delegate: Delegate?) : KSAdapter() {
    interface Delegate :
        ErroredBackingViewHolder.Delegate,
        FriendBackingViewHolder.Delegate,
        ProjectStateChangedPositiveViewHolder.Delegate,
        ProjectStateChangedViewHolder.Delegate,
        ProjectUpdateViewHolder.Delegate,
        EmptyActivityFeedViewHolder.Delegate

    fun takeActivities(activities: List<Activity?>) {
        setSection(SECTION_ACTIVITIES_VIEW, activities)
        notifyDataSetChanged()
    }

    fun takeErroredBackings(erroredBackings: List<ErroredBacking?>) {
        if (erroredBackings.isEmpty()) {
            setSection(SECTION_ERRORED_BACKINGS_HEADER_VIEW, emptyList<Any>())
            setSection(SECTION_ERRORED_BACKINGS_VIEW, emptyList<Any>())
        } else {
            setSection(SECTION_ERRORED_BACKINGS_HEADER_VIEW, listOf(erroredBackings.size))
            setSection(SECTION_ERRORED_BACKINGS_VIEW, erroredBackings)
        }
        notifyDataSetChanged()
    }

    fun takeSurveys(surveyResponses: List<SurveyResponse?>) {
        if (surveyResponses.isNotEmpty()) {
            setSection(SECTION_SURVEYS_HEADER_VIEW, listOf(surveyResponses.size))
            setSection(SECTION_SURVEYS_VIEW, surveyResponses)
        } else {
            setSection(SECTION_SURVEYS_HEADER_VIEW, emptyList<Any>())
            setSection(SECTION_SURVEYS_VIEW, emptyList<Any>())
        }
        notifyDataSetChanged()
    }

    fun showLoggedInEmptyState(show: Boolean) {
        setSection(SECTION_LOGGED_IN_EMPTY_VIEW, if (show) listOf(true) else ListUtils.empty())
        notifyDataSetChanged()
    }

    fun showLoggedOutEmptyState(show: Boolean) {
        setSection(SECTION_LOGGED_OUT_EMPTY_VIEW, if (show) listOf(false) else ListUtils.empty())
        notifyDataSetChanged()
    }

    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        when (sectionRow.section()) {
            SECTION_LOGGED_IN_EMPTY_VIEW -> return R.layout.empty_activity_feed_view
            SECTION_LOGGED_OUT_EMPTY_VIEW -> return R.layout.empty_activity_feed_view
            SECTION_ERRORED_BACKINGS_HEADER_VIEW -> return R.layout.item_header_errored_backings
            SECTION_ERRORED_BACKINGS_VIEW -> return R.layout.item_errored_backing
            SECTION_SURVEYS_HEADER_VIEW -> return R.layout.activity_survey_header_view
            SECTION_SURVEYS_VIEW -> return R.layout.activity_survey_view
            SECTION_ACTIVITIES_VIEW -> return getActivityLayoutId(sectionRow)
        }
        return R.layout.empty_view
    }

    private fun getActivityLayoutId(sectionRow: SectionRow): Int {
        if (objectFromSectionRow(sectionRow) is Activity) {
            val activity = objectFromSectionRow(sectionRow) as Activity
            when (activity.category()) {
                Activity.CATEGORY_BACKING -> return R.layout.activity_friend_backing_view
                Activity.CATEGORY_FOLLOW -> return R.layout.activity_friend_follow_view
                Activity.CATEGORY_FAILURE, Activity.CATEGORY_CANCELLATION, Activity.CATEGORY_SUSPENSION -> return R.layout.activity_project_state_changed_view
                Activity.CATEGORY_LAUNCH, Activity.CATEGORY_SUCCESS -> return R.layout.activity_project_state_changed_positive_view
                Activity.CATEGORY_UPDATE -> return R.layout.activity_project_update_view
            }
        }
        return R.layout.empty_view
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.activity_survey_header_view -> SurveyHeaderViewHolder(
                ActivitySurveyHeaderViewBinding
                    .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            )

            R.layout.activity_survey_view -> SurveyViewHolder(
                ActivitySurveyViewBinding
                    .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            )

            R.layout.activity_friend_backing_view -> FriendBackingViewHolder(
                ActivityFriendBackingViewBinding
                    .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false),
                delegate
            )

            R.layout.activity_friend_follow_view -> FriendFollowViewHolder(
                ActivityFriendFollowViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false
                )
            )

            R.layout.activity_project_state_changed_view -> ProjectStateChangedViewHolder(
                ActivityProjectStateChangedViewBinding
                    .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false),
                delegate
            )

            R.layout.activity_project_state_changed_positive_view -> ProjectStateChangedPositiveViewHolder(
                ActivityProjectStateChangedPositiveViewBinding
                    .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false),
                delegate
            )

            R.layout.activity_project_update_view -> ProjectUpdateViewHolder(ActivityProjectUpdateViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
            R.layout.empty_activity_feed_view -> EmptyActivityFeedViewHolder(EmptyActivityFeedViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
            R.layout.item_errored_backing -> ErroredBackingViewHolder(ItemErroredBackingBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    companion object {
        private const val SECTION_LOGGED_IN_EMPTY_VIEW = 0
        private const val SECTION_LOGGED_OUT_EMPTY_VIEW = 1
        private const val SECTION_ERRORED_BACKINGS_HEADER_VIEW = 2
        private const val SECTION_ERRORED_BACKINGS_VIEW = 3
        private const val SECTION_SURVEYS_HEADER_VIEW = 4
        private const val SECTION_SURVEYS_VIEW = 5
        private const val SECTION_ACTIVITIES_VIEW = 6
    }

    init {
        insertSection(SECTION_LOGGED_IN_EMPTY_VIEW, emptyList<Any>())
        insertSection(SECTION_LOGGED_OUT_EMPTY_VIEW, emptyList<Any>())
        insertSection(SECTION_ERRORED_BACKINGS_HEADER_VIEW, emptyList<Any>())
        insertSection(SECTION_ERRORED_BACKINGS_VIEW, emptyList<Any>())
        insertSection(SECTION_SURVEYS_HEADER_VIEW, emptyList<Any>())
        insertSection(SECTION_SURVEYS_VIEW, emptyList<Any>())
        insertSection(SECTION_ACTIVITIES_VIEW, emptyList<Any>())
    }
}
