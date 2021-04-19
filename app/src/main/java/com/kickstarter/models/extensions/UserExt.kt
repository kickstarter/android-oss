@file:JvmName("UserExt")
package com.kickstarter.models.extensions

import com.kickstarter.libs.utils.I18nUtils
import com.kickstarter.models.User

/**
 * Check if the user email has been verified
 *
 * @return true if the userEmail has been verified
 *         false if the userEmail has not been verified
 *         false if the field isEmailVerified does not exist
 */
fun User.isUserEmailVerified() = this.isEmailVerified ?: false

/**
 * Returns whether the user's location setting is in Germany.
 */
fun User.isLocationGermany(): Boolean {
    val location = this.location() ?: return false
    return I18nUtils.isCountryGermany(location.country())
}

/**
 * Returns the sum of created projects and draft projects from the user payload.
 */
fun User.getCreatedAndDraftProjectsCount(): Int {
    return (this.createdProjectsCount() ?: 0) + (this.draftProjectsCount() ?: 0)
}
