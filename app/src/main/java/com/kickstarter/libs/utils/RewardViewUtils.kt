package com.kickstarter.libs.utils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.models.Reward

object RewardViewUtils {

    /**
     * Returns the drawable resource ID of the check background based on project status.
     */
    @DrawableRes
    fun checkBackgroundDrawable(project: Project): Int {
        return if (project.isLive) {
            R.drawable.circle_blue_alpha_6
        } else {
            R.drawable.circle_grey_300
        }
    }

    /**
     * Returns the color resource ID of the rewards button based on project and if user has backed reward.
     */
    @ColorRes
    fun pledgeButtonColor(project: Project, reward: Reward): Int {
        return if (BackingUtils.isBacked(project, reward) && project.isLive) {
            R.color.button_pledge_manage
        } else if (!project.isLive) {
            R.color.button_pledge_ended
        } else {
            R.color.button_pledge_live
        }
    }

    /**
     * Returns the string resource ID of the rewards button based on project and reward status.
     */
    @StringRes
    fun pledgeButtonAlternateText(project: Project, reward: Reward): Int {
        return if (BackingUtils.isBacked(project, reward) && project.isLive) {
            R.string.Manage_your_pledge
        } else if (BackingUtils.isBacked(project, reward) && !project.isLive) {
            R.string.View_your_pledge
        } else if (RewardUtils.isAvailable(project, reward) && project.isBacking) {
            R.string.Select_this_instead
        } else if (!RewardUtils.isAvailable(project, reward)) {
            R.string.No_longer_available
        } else {
            throw IllegalStateException()
        }
    }
}

