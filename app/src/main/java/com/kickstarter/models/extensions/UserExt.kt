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

/**
 * Defines the possible Email opt-in options
 */
enum class Email(val field: String) {
    EMAIL_BACKINGS("notify_of_backings"),
    EMAIL_UPDATES("notify_of_updates"),
    EMAIL_FOLLOWER("notify_of_follower"),
    EMAIL_FRIEND_ACTIVITY("notify_of_friend_activity"),
    EMAIL_NOTIFY_COMMENT("notify_of_comments"),
    EMAIL_CREATOR_EDU("notify_of_creator_edu"),
    EMAIL_CREATOR_DIG("notify_of_creator_digest"),
    EMAIL_MESSAGE("notify_of_messages"),
    EMAIL_REPLAY("notify_of_comment_replies")
}

/**
 * Defines the possible Push Notification opt-in options
 */
enum class PushNotification(val field: String) {
    PUSH_BACKINGS("notify_mobile_of_backings"),
    PUSH_UPDATES("notify_mobile_of_updates"),
    PUSH_FOLLOWER("notify_mobile_of_follower"),
    PUSH_FRIEND_ACTIVITY("notify_mobile_of_friend_activity"),
    PUSH_NOTIFY_COMMENT("notify_mobile_of_comments"),
    PUSH_LIKE("notify_mobile_of_post_likes"),
    PUSH_MESSAGE("notify_mobile_of_messages")
}
