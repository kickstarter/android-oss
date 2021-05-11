package com.kickstarter.libs.braze

import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.utils.ConfigFeatureName
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User
import rx.schedulers.Schedulers

class InAppCustomListenerHandler(
    private val currentUser: CurrentUserType,
    private val currentConfig: CurrentConfigType
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
}
