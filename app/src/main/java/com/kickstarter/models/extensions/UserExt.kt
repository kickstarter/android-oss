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
 * Returns the traits we currently send with the Identify calls
 */
fun User.getTraits() = mapOf(
    ID to this.id(),
    NAME to this.name(),
    Email.EMAIL_BACKINGS.field to this.notifyOfBackings(),
    Email.EMAIL_UPDATES.field to this.notifyOfUpdates(),
    Email.EMAIL_FOLLOWER.field to this.notifyOfFollower(),
    Email.EMAIL_FRIEND_ACTIVITY.field to this.notifyOfFriendActivity(),
    Email.EMAIL_NOTIFY_COMMENT.field to this.notifyMobileOfComments(),
    Email.EMAIL_CREATOR_DIG.field to this.notifyOfCreatorDigest(),
    Email.EMAIL_CREATOR_EDU.field to this.notifyOfCreatorEdu(),
    Email.EMAIL_MESSAGE.field to this.notifyOfMessages(),
    Email.EMAIL_REPLAY.field to this.notifyOfCommentReplies(),
    PushNotification.PUSH_BACKINGS.field to this.notifyMobileOfBackings(),
    PushNotification.PUSH_UPDATES.field to this.notifyMobileOfUpdates(),
    PushNotification.PUSH_FOLLOWER.field to this.notifyMobileOfFollower(),
    PushNotification.PUSH_FRIEND_ACTIVITY.field to this.notifyMobileOfFriendActivity(),
    PushNotification.PUSH_NOTIFY_COMMENT.field to this.notifyMobileOfComments(),
    PushNotification.PUSH_LIKE.field to this.notifyMobileOfPostLikes(),
    PushNotification.PUSH_MESSAGE.field to this.notifyMobileOfMessages(),
    PushNotification.PUSH_MARKETING.field to this.notifyMobileOfMarketingUpdate()
)

const val NAME = "name"
const val ID = "id"

/**
 * Defines the possible Email opt-in options
 * @param field equivalent name on the User endpoint field,
 * check @link /v1/users/self -> https://staging.kickstarter.com/admin/api-docs/index.html
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
 * @param field equivalent name on the User endpoint field
 * check @link /v1/users/self -> https://staging.kickstarter.com/admin/api-docs/index.html
 */
enum class PushNotification(val field: String) {
    PUSH_BACKINGS("notify_mobile_of_backings"),
    PUSH_UPDATES("notify_mobile_of_updates"),
    PUSH_FOLLOWER("notify_mobile_of_follower"),
    PUSH_FRIEND_ACTIVITY("notify_mobile_of_friend_activity"),
    PUSH_NOTIFY_COMMENT("notify_mobile_of_comments"),
    PUSH_LIKE("notify_mobile_of_post_likes"),
    PUSH_MESSAGE("notify_mobile_of_messages"),
    PUSH_MARKETING("notify_mobile_of_marketing_update")
}
