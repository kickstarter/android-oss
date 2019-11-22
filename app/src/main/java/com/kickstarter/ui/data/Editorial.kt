package com.kickstarter.ui.data

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kickstarter.R
import java.io.Serializable

data class Editorial(@StringRes val ctaTitle: Int,
                     @StringRes val ctaDescription: Int,
                     @StringRes val title: Int,
                     @StringRes val description: Int,
                     @ColorRes val backgroundColor: Int,
                     @DrawableRes val graphic: Int,
                     val tagId: Int) : Serializable {

    companion object {
        val GO_REWARDLESS = Editorial(R.string.Back_it_because_you_believe_in_it,
                R.string.Find_projects_that_speak_to_you,
                R.string.Join_us_in_supporting_creative_work_for_its_own_sake_this_holiday_season,
                R.string.These_projects_could_use_your_support,
                R.color.trust_700,
                R.drawable.go_rewardless_header,
                518)
    }
}
