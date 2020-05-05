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
    LIGHTS_ON(R.string.lights_on_title,
            R.string.lights_on_description,
            R.string.inside_lights_on_title,
            R.string.inside_lights_on_description,
            R.color.white,
            R.drawable.lights_on,
            557);
}
