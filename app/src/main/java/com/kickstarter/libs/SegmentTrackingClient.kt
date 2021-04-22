package com.kickstarter.libs

import android.content.Context
import com.kickstarter.models.User
import com.kickstarter.models.extensions.Email
import com.kickstarter.models.extensions.PushNotification
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
                Timber.d("Queued ${type().tag} Identify userName: ${this.name()} userId: ${this.id()} traits: ${getTraits(user)}")
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
        this[Email.EMAIL_BACKINGS.field] = user.notifyOfBackings()
        this[Email.EMAIL_UPDATES.field] = user.notifyOfUpdates()
        this[Email.EMAIL_FOLLOWER.field] = user.notifyOfFollower()
        this[Email.EMAIL_FRIEND_ACTIVITY.field] = user.notifyOfFriendActivity()
        this[Email.EMAIL_NOTIFY_COMMENT.field] = user.notifyOfComments()
        this[Email.EMAIL_CREATOR_EDU.field] = user.notifyOfCreatorEdu()
        this[Email.EMAIL_CREATOR_DIG.field] = user.notifyOfCreatorDigest()
        this[Email.EMAIL_MESSAGE.field] = user.notifyOfMessages()
        this[Email.EMAIL_REPLAY.field] = user.notifyOfCommentReplies()
        // - Push Notifications related subscriptions
        this[PushNotification.PUSH_BACKINGS.field] = user.notifyMobileOfBackings()
        this[PushNotification.PUSH_UPDATES.field] = user.notifyMobileOfUpdates()
        this[PushNotification.PUSH_FOLLOWER.field] = user.notifyMobileOfFollower()
        this[PushNotification.PUSH_FRIEND_ACTIVITY.field] = user.notifyMobileOfFriendActivity()
        this[PushNotification.PUSH_NOTIFY_COMMENT.field] = user.notifyMobileOfComments()
        this[PushNotification.PUSH_MESSAGE.field] = user.notifyMobileOfMessages()
        this[PushNotification.PUSH_LIKE.field] = user.notifyMobileOfPostLikes()
    }
}
