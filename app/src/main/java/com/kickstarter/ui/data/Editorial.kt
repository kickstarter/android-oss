package com.kickstarter.ui.data

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kickstarter.R

enum class Editorial(@StringRes val ctaTitle: Int,
                     @StringRes val ctaDescription: Int,
                     @StringRes val title: Int,
                     @StringRes val description: Int,
                     @ColorRes val backgroundColor: Int,
                     @DrawableRes val graphic: Int,
                     val tagId: Int) {

    GO_REWARDLESS(R.string.Back_it_because_you_believe_in_it,
            R.string.Find_projects_that_speak_to_you,
            R.string.This_holiday_season_support_a_project_for_no_reward,
            R.string.These_projects_could_use_your_support,
            R.color.trust_700,
            R.drawable.go_rewardless_header,
            518),
    LIGHTS_ON(R.string.Introducing_Lights_On,
            R.string.Support_creative_spaces_and_businesses_affected_by,
            R.string.Show_up_for_the_spaces_you_love,
            R.string.Help_local_businesses_keep_the_lights,
            R.color.white,
            R.drawable.lights_on,
            557);
}
