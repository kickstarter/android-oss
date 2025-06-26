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

    val isEditPledgeAllowed = ffClient.getBoolean(FlagKey.ANDROID_PLOT_EDIT_PLEDGE)
    return ManagePledgeMenuOptions(
        showEditPledge = project.isLive && !isBackingStatusPreAuth && isEditPledgeAllowed,
        showUpdatePayment = project.isLive && !isBackingStatusPreAuth,
        showSeeRewards = !project.isLive,
        showCancelPledge = project.isLive && !isBackingStatusPreAuth,
        showContactCreator = true
    )
}
