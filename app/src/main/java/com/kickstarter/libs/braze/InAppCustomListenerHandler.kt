package com.kickstarter.libs.braze

import android.net.Uri
import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User

class InAppCustomListenerHandler(
    private val loggedInUser: User?,
    private val config: Config?
) {

    fun shouldShowMessage() =
        if (config != null && loggedInUser != null) {
            config.isFeatureFlagEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName)
        } else false

    fun validateDataWith(id: Int, uri: Uri?) {
        TODO("Not yet implemented")
    }
}
