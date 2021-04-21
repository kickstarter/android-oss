package com.kickstarter.libs

import android.content.Context
import com.kickstarter.models.User
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_BACKINGS
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_CREATOR_DIG
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_CREATOR_EDU
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_FOLLOWER
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_FRIEND_ACTIVITY
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_MESSAGE
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_NOTIFY_COMMENT
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_REPLAY
import com.kickstarter.ui.data.OptInSubscriptionTypes.Email.EMAIL_UPDATES
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_BACKINGS
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_FOLLOWER
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_FRIEND_ACTIVITY
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_LIKE
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_MESSAGE
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_NOTIFY_COMMENT
import com.kickstarter.ui.data.OptInSubscriptionTypes.PushNotification.PUSH_UPDATES
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits
import timber.log.Timber

class SegmentTrackingClient(
    build: Build,
    context: Context,
    currentConfig: CurrentConfigType,
    currentUser: CurrentUserType,
    optimizely: ExperimentsClientType,
    private val segmentAnalytics: Analytics?
) : TrackingClient(context, currentUser, build, currentConfig, optimizely) {

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>) {
        segmentAnalytics?.let { segment ->
            segment.track(eventName, this.getProperties(newProperties))
        }
    }

    /**
     * In order to send custom properties to segment we need to use
     * the method Properties() from the Segment SDK
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#track
     */
    private fun getProperties(newProperties: Map<String, Any?>) = Properties().apply {
        newProperties.forEach { (key, value) ->
            this[key] = value
        }
    }

    override fun type() = Type.SEGMENT

    /**
     * Perform the request to the Segment third party library
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#identify
     */
    override fun identify(user: User) {
        super.identify(user)

        if (this.build.isDebug && type() == Type.SEGMENT) {
            user.apply {
                Timber.d("Queued ${type().tag} Identify userName: ${this.name()} userId: ${ this.id()}")
            }
        }
        segmentAnalytics?.let { segment ->
            segment.identify(user.id().toString(), getTraits(user), null)
        }
    }

    /**
     * clears the internal stores on Segment SDK for the current user and group
     * https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#reset
     */
    override fun reset() {
        super.reset()
        if (this.build.isDebug) {
            Timber.d("Queued ${type().tag} Reset user after logout")
        }
        segmentAnalytics?.reset()
    }

    /**
     * In order to send custom properties to segment for the Identify method we need to use
     * the method Traits() from the Segment SDK
     * see https://segment.com/docs/connections/sources/catalog/libraries/mobile/android/#identify
     *
     * Added as trait the user name
     * Added as traits the user preferences for Email and Push Notifications Subscriptions
     */
    private fun getTraits(user: User) = Traits().apply {
        this.putName(user.name())
        // - Email related subscriptions
        this[EMAIL_BACKINGS.name] = user.notifyOfBackings()
        this[EMAIL_UPDATES.name] = user.notifyOfUpdates()
        this[EMAIL_FOLLOWER.name] = user.notifyOfFollower()
        this[EMAIL_FRIEND_ACTIVITY.name] = user.notifyOfFriendActivity()
        this[EMAIL_NOTIFY_COMMENT.name] = user.notifyOfComments()
        this[EMAIL_CREATOR_EDU.name] = user.notifyOfCreatorEdu()
        this[EMAIL_CREATOR_DIG.name] = user.notifyOfCreatorDigest()
        this[EMAIL_MESSAGE.name] = user.notifyOfMessages()
        this[EMAIL_REPLAY.name] = user.notifyOfCommentReplies()
        // - Push Notifications related subscriptions
        this[PUSH_BACKINGS.name] = user.notifyMobileOfBackings()
        this[PUSH_UPDATES.name] = user.notifyMobileOfUpdates()
        this[PUSH_FOLLOWER.name] = user.notifyMobileOfFollower()
        this[PUSH_FRIEND_ACTIVITY.name] = user.notifyMobileOfFriendActivity()
        this[PUSH_NOTIFY_COMMENT.name] = user.notifyMobileOfComments()
        this[PUSH_MESSAGE.name] = user.notifyMobileOfMessages()
        this[PUSH_LIKE.name] = user.notifyMobileOfPostLikes()
    }
}
