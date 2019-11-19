package com.kickstarter.ui.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kickstarter.R

data class Editorial(@StringRes val ctaTitle: Int, @StringRes val ctaDescription: Int,
                     @StringRes val title: Int, @StringRes val description: Int,
                     @DrawableRes val background: Int, val tag: Int) {

    companion object {
        val GO_REWARDLESS = Editorial(R.string.app_name, R.string.app_name,
                R.string.app_name, R.string.app_name,
                R.drawable.rect_white_rounded, 55)
    }
}
