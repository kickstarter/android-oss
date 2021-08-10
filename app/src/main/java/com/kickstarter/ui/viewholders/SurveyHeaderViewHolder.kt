package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ActivitySurveyHeaderViewBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ObjectUtils

class SurveyHeaderViewHolder(private val binding: ActivitySurveyHeaderViewBinding) :
    KSViewHolder(binding.root) {
    private val ksString: KSString = environment().ksString()

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val unansweredSurveyCount = ObjectUtils.requireNonNull(data as Int)
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
