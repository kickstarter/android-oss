package com.kickstarter.libs.braze

import android.net.Uri
import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType

class InAppCustomListenerHandler(
    private val loggedInUser: User?,
    private val config: Config?,
    private val apiClientType: ApiClientType
) {

    fun shouldShowMessage() =
        if (config != null && loggedInUser != null) {
            config.isFeatureFlagEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
        } else false

    fun validateDataWith(id: Int, uri: Uri?) {
        // TODO: Check the id and url are the valid ones
        this.loggedInUser?.let { user ->
            if (user.notifyMobileOfMarketingUpdate() == false) {
                val updatedUser = user.toBuilder().notifyMobileOfMarketingUpdate(true).build()
                updateSettings(updatedUser)
            }
        }
    }

    private fun updateSettings(user: User) {
        this.apiClientType.updateUserSettings(user)
            .materialize()
            .share()
    }
}
