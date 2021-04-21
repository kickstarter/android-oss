package com.kickstarter.ui.data

/**
 * We currently have two types of subscriptions for the user
 * - Email
 * - Push Notifications
 */
class OptInSubscriptionTypes {

    /**
     * Defines the possible Email opt-in options
     */
    enum class Email(private val field: String) {
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
    enum class PushNotification(private val field: String) {
        PUSH_BACKINGS("notify_mobile_of_backings"),
        PUSH_UPDATES("notify_mobile_of_updates"),
        PUSH_FOLLOWER("notify_mobile_of_follower"),
        PUSH_FRIEND_ACTIVITY("notify_mobile_of_friend_activity"),
        PUSH_NOTIFY_COMMENT("notify_mobile_of_comments"),
        PUSH_LIKE("notify_mobile_of_post_likes"),
        PUSH_MESSAGE("notify_mobile_of_messages")
    }
}
