package com.kickstarter.ui.helpers

import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ManagePledgeMenuOptions

/**
 * Factory for creating [ManagePledgeMenuOptions] instances.
 * This class determines which menu options should be visible based on the project and backing status.
 *
 * @param project The project for which to create menu options.
 * @param ffClient The feature flag client used to check feature flag s statuses.
 * @return A [ManagePledgeMenuOptions] instance with the appropriate visibility settings for each menu item.
 */
fun createManagePledgeMenuOptions(
    project: Project,
    ffClient: FeatureFlagClientType
): ManagePledgeMenuOptions {
    val backing = project.backing()
    val isBackingStatusPreAuth = backing?.status() == Backing.STATUS_PREAUTH

    val isFeatureFlagOn = ffClient.getBoolean(FlagKey.ANDROID_PLOT_EDIT_PLEDGE) == true
    val isPlotProject = project.isPledgeOverTimeAllowed() == true // If the project allows pledge over time, it is considered a plot project.
    val isPledgeOverTime = !backing?.paymentIncrements.isNullOrEmpty() // If the backing has payment increments, it is considered a pledge over time.
    val showEditPledge = when {
        isFeatureFlagOn && isPlotProject -> project.isLive && !isBackingStatusPreAuth
        else -> false
    }

    val showChooseAnotherReward = when {
        !isFeatureFlagOn -> !isPledgeOverTime
        isFeatureFlagOn -> !isPlotProject
        else -> false
    }

    return ManagePledgeMenuOptions(
        showEditPledge = showEditPledge,
        showChooseAnotherReward = showChooseAnotherReward,
        showUpdatePayment = project.isLive && !isBackingStatusPreAuth,
        showSeeRewards = !project.isLive,
        showCancelPledge = project.isLive && !isBackingStatusPreAuth,
        showContactCreator = true
    )
}
