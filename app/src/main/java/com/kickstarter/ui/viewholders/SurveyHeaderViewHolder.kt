package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ActivitySurveyHeaderViewBinding

class SurveyHeaderViewHolder(private val binding: ActivitySurveyHeaderViewBinding) :
    KSViewHolder(binding.root) {
    private val ksString = requireNotNull(environment().ksString())

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val unansweredSurveyCount = requireNotNull(data as Int)
        if (unansweredSurveyCount > 0) {
            binding.activitySurveyHeaderTextView.text = ksString.format(
                "Reward_Surveys",
                unansweredSurveyCount,
                "reward_survey_count",
                unansweredSurveyCount
                    .toString()
            )
        }
    }
}
