package com.kickstarter.libs.braze

import android.net.Uri
import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import rx.schedulers.Schedulers

class InAppCustomListenerHandler(
    private val currentUser: CurrentUserType,
    private val currentConfig: CurrentConfigType,
    private val apiClientType: ApiClientType
) {

    private var config: Config? = null
    private var loggedInUser: User? = null

    init {
        this.currentConfig.observable()
            .subscribeOn(Schedulers.io())
            .subscribe {
                this.config = it
            }

        this.currentUser.observable()
            .distinctUntilChanged()
            .subscribe {
                this.loggedInUser = it
            }
    }

    fun shouldShowMessage() =
        if (this.config != null && this.loggedInUser != null) {
            this.config?.isFeatureFlagEnabled(ConfigFeatureName.BRAZE_ENABLED.configFeatureName) ?: false
        } else false

    fun validateDataWith(id: Int, uri: Uri?) {
        // TODO: Check the id and url are the valid ones
        this.loggedInUser?.let { user ->
            val updatedUser = user.toBuilder().notifyMobileOfMarketingUpdate(true).build()
            updateSettings(updatedUser)
        }
    }

    private fun updateSettings(user: User) {
        this.apiClientType.updateUserSettings(user)
    }
}
