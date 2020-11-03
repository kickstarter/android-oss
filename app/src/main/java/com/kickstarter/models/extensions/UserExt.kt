@file:JvmName("UserExt")
package com.kickstarter.models.extensions

import com.kickstarter.models.User

/**
 * Check if the user email has been verified
 *
 * @return true if the userEmail has been verified
 *         false if the userEmail has not been verified
 *         false if the field isEmailVerified does not exist
 */
fun User.isUserEmailVerified() = this.isEmailVerified ?: false
