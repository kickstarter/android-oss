package com.kickstarter.ui.data

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kickstarter.R

data class Editorial(@StringRes val ctaTitle: Int,
                     @StringRes val ctaDescription: Int,
                     @StringRes val title: Int,
                     @StringRes val description: Int,
                     @ColorRes val backgroundTint: Int,
                     @DrawableRes val graphic: Int?,
                     val tagId: Int) {

    companion object {
        val GO_REWARDLESS = Editorial(R.string.Back_it_because_you_believe_in_it,
                R.string.Find_projects_that_speak_to_you,
                R.string.Join_us_in_supporting_creative_work_for_its_own_sake_this_holiday_season,
                R.string.Use_the_hashtag_hashtag_backeditbecause_to_share_what_projects_youre_supporting_and_why_theyre_important_to_you,
                R.color.trust_700,
                null,
                518)
    }
}
