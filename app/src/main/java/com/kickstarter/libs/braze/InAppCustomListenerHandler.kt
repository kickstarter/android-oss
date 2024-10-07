package com.kickstarter.libs.braze

import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.models.User

class InAppCustomListenerHandler(
    private val currentUser: CurrentUserTypeV2
) {
    private var loggedInUser: User? = null

    init {

        this.currentUser.observable()
            .filter { it.isPresent() }
            .map { requireNotNull(it.getValue()) }
            .distinctUntilChanged()
            .map {
                this.loggedInUser = it
            }
            .subscribe()
    }

    /**
     * In case the user is logged in
     * @return true
     *
     * In case no user logged in
     * @return false
     */
    fun shouldShowMessage() = this.loggedInUser != null
}
