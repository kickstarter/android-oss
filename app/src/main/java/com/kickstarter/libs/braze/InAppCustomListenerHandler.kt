package com.kickstarter.libs.braze

import com.kickstarter.libs.CurrentUserType
import com.kickstarter.models.User

class InAppCustomListenerHandler(
    private val currentUser: CurrentUserType
) {
    private var loggedInUser: User? = null

    init {

        this.currentUser.observable()
            .distinctUntilChanged()
            .subscribe {
                this.loggedInUser = it
            }
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
