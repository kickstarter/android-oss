package com.kickstarter.libs.utils

import android.view.View
import android.widget.Button
import androidx.annotation.ColorRes
import com.kickstarter.R
import com.kickstarter.models.Project

object ProjectViewUtils {

    /**
     * Returns the color resource ID of the rewards button based on project and backing status.
     */
    @ColorRes
    fun pledgeButtonColor(project: Project): Int {
        return if (project.isBacking && project.isLive) {
            R.color.button_pledge_manage
        } else if (project.isBacking && !project.isLive) {
            //todo: view rewards will be black
            R.color.button_pledge_ended
        } else {
            R.color.button_pledge_live
        }
    }

    /**
     * Set correct button view based on project and backing status.
     */
    @JvmStatic
    fun setActionButton(project: Project, backProjectButton: Button,
                        managePledgeButton: Button, viewPledgeButton: Button) {
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
    }
}