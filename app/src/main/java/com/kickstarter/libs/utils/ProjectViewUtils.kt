package com.kickstarter.libs.utils

import android.view.View
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.models.Project

object ProjectViewUtils {

    /**
     * Returns the color resource ID of the rewards button based on project and backing status.
     */
    @ColorRes
    fun rewardsButtonColor(project: Project): Int {
        return if (project.isBacking && project.isLive) {
            R.color.button_pledge_manage
        } else if (!project.isLive) {
            R.color.button_pledge_ended
        } else {
            R.color.button_pledge_live
        }
    }

    fun rewardsButtonText(project: Project): Int {
        return if (!project.isBacking && project.isLive) {
            R.string.Back_this_project
        } else if (project.isBacking && project.isLive) {
            R.string.Manage
        } else if (project.isBacking && !project.isLive) {
            R.string.View_your_pledge
        } else {
            R.string.View_rewards
        }
    }

    @StringRes
    fun rewardsToolbarTitle(project: Project): Int {
        return if (!project.isBacking && project.isLive) {
            R.string.Back_this_project
        } else if (project.isBacking && project.isLive) {
            R.string.Manage_your_pledge
        } else if (project.isBacking && !project.isLive) {
            R.string.View_your_pledge
        } else {
            R.string.View_rewards
        }
    }

    /**
     * Set correct button view based on project and backing status.
     */
    @JvmStatic
    fun setActionButton(project: Project, backProjectButton: Button,
                        managePledgeButton: Button, viewPledgeButton: Button,
                        viewRewardsButton: Button?) {

        if (!project.isBacking && project.isLive) {
            backProjectButton.visibility = View.VISIBLE
        } else {
            backProjectButton.visibility = View.GONE
        }

        if (project.isBacking && project.isLive) {
            managePledgeButton.visibility = View.VISIBLE
        } else {
            managePledgeButton.visibility = View.GONE
        }

        if (project.isBacking && !project.isLive) {
            viewPledgeButton.visibility = View.VISIBLE
        } else {
            viewPledgeButton.visibility = View.GONE
        }

        viewRewardsButton?.let {
            if (!project.isBacking && !project.isLive) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }
}
