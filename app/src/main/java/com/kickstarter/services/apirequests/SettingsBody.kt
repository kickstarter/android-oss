package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SettingsBody internal constructor(
    private val optedOutOfRecommendations: Int,
    private val notifyMobileOfBackings: Boolean,
    private val notifyMobileOfComments: Boolean,
    private val notifyMobileOfCreatorEdu: Boolean,
    private val notifyMobileOfFollower: Boolean,
    private val notifyMobileOfFriendActivity: Boolean,
    private val notifyMobileOfMessages: Boolean,
    private val notifyMobileOfPostLikes: Boolean,
    private val notifyMobileOfUpdates: Boolean,
    private val notifyMobileOfMarketingUpdate: Boolean,
    private val notifyOfBackings: Boolean,
    private val notifyOfComments: Boolean,
    private val notifyOfCommentReplies: Boolean,
    private val notifyOfCreatorDigest: Boolean,
    private val notifyOfCreatorEdu: Boolean,
    private val notifyOfFollower: Boolean,
    private val notifyOfFriendActivity: Boolean,
    private val notifyOfMessages: Boolean,
    private val notifyOfUpdates: Boolean,
    private val showPublicProfile: Int,
    private val social: Int,
    private val alumniNewsletter: Int,
    private val artsCultureNewsletter: Int,
    private val filmNewsletter: Int,
    private val gamesNewsletter: Int,
    private val happeningNewsletter: Int,
    private val inventNewsletter: Int,
    private val musicNewsletter: Int,
    private val promoNewsletter: Int,
    private val publishingNewsletter: Int,
    private val weeklyNewsletter: Int
) : Parcelable {
    fun optedOutOfRecommendations() = this.optedOutOfRecommendations
    fun notifyMobileOfBackings() = this.notifyMobileOfBackings
    fun notifyMobileOfComments() = this.notifyMobileOfComments
    fun notifyMobileOfCreatorEdu() = this.notifyMobileOfCreatorEdu
    fun notifyMobileOfFollower() = this.notifyMobileOfFollower
    fun notifyMobileOfFriendActivity() = this.notifyMobileOfFriendActivity
    fun notifyMobileOfMessages() = this.notifyMobileOfMessages
    fun notifyMobileOfPostLikes() = this.notifyMobileOfPostLikes
    fun notifyMobileOfUpdates() = this.notifyMobileOfUpdates
    fun notifyMobileOfMarketingUpdate() = this.notifyMobileOfMarketingUpdate
    fun notifyOfBackings() = this.notifyOfBackings
    fun notifyOfComments() = this.notifyOfComments
    fun notifyOfCommentReplies() = this.notifyOfCommentReplies
    fun notifyOfCreatorDigest() = this.notifyOfCreatorDigest
    fun notifyOfCreatorEdu() = this.notifyOfCreatorEdu
    fun notifyOfFollower() = this.notifyOfFollower
    fun notifyOfFriendActivity() = this.notifyOfFriendActivity
    fun notifyOfMessages() = this.notifyOfMessages
    fun notifyOfUpdates() = this.notifyOfUpdates
    fun showPublicProfile() = this.showPublicProfile
    fun social() = this.social
    fun alumniNewsletter() = this.alumniNewsletter
    fun artsCultureNewsletter() = this.artsCultureNewsletter
    fun filmNewsletter() = this.filmNewsletter
    fun gamesNewsletter() = this.gamesNewsletter
    fun happeningNewsletter() = this.happeningNewsletter
    fun inventNewsletter() = this.inventNewsletter
    fun musicNewsletter() = this.musicNewsletter
    fun promoNewsletter() = this.promoNewsletter
    fun publishingNewsletter() = this.publishingNewsletter
    fun weeklyNewsletter() = this.weeklyNewsletter

    @Parcelize
    data class Builder(
        private var optedOutOfRecommendations: Int = 0,
        private var notifyMobileOfBackings: Boolean = false,
        private var notifyMobileOfComments: Boolean = false,
        private var notifyMobileOfCreatorEdu: Boolean = false,
        private var notifyMobileOfFollower: Boolean = false,
        private var notifyMobileOfFriendActivity: Boolean = false,
        private var notifyMobileOfMessages: Boolean = false,
        private var notifyMobileOfPostLikes: Boolean = false,
        private var notifyMobileOfUpdates: Boolean = false,
        private var notifyMobileOfMarketingUpdate: Boolean = false,
        private var notifyOfBackings: Boolean = false,
        private var notifyOfComments: Boolean = false,
        private var notifyOfCommentReplies: Boolean = false,
        private var notifyOfCreatorDigest: Boolean = false,
        private var notifyOfCreatorEdu: Boolean = false,
        private var notifyOfFollower: Boolean = false,
        private var notifyOfFriendActivity: Boolean = false,
        private var notifyOfMessages: Boolean = false,
        private var notifyOfUpdates: Boolean = false,
        private var showPublicProfile: Int = 0,
        private var social: Int = 0,
        private var alumniNewsletter: Int = 0,
        private var artsCultureNewsletter: Int = 0,
        private var filmNewsletter: Int = 0,
        private var gamesNewsletter: Int = 0,
        private var happeningNewsletter: Int = 0,
        private var inventNewsletter: Int = 0,
        private var musicNewsletter: Int = 0,
        private var promoNewsletter: Int = 0,
        private var publishingNewsletter: Int = 0,
        private var weeklyNewsletter: Int = 0
    ) : Parcelable {
        fun optedOutOfRecommendations(optedOutOfRecommendations: Int?) =
            apply { this.optedOutOfRecommendations = optedOutOfRecommendations ?: 0 }

        fun notifyMobileOfBackings(notifyMobileOfBackings: Boolean?) =
            apply { this.notifyMobileOfBackings = notifyMobileOfBackings ?: false }

        fun notifyMobileOfComments(notifyMobileOfComments: Boolean?) =
            apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }

        fun notifyMobileOfCreatorEdu(notifyMobileOfCreatorEdu: Boolean?) =
            apply { this.notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu ?: false }

        fun notifyMobileOfFollower(notifyMobileOfFollower: Boolean?) =
            apply { this.notifyMobileOfFollower = notifyMobileOfFollower ?: false }

        fun notifyMobileOfFriendActivity(notifyMobileOfFriendActivity: Boolean?) =
            apply { this.notifyMobileOfFriendActivity = notifyMobileOfFriendActivity ?: false }

        fun notifyMobileOfMessages(notifyMobileOfMessages: Boolean?) =
            apply { this.notifyMobileOfMessages = notifyMobileOfMessages ?: false }

        fun notifyMobileOfPostLikes(notifyMobileOfPostLikes: Boolean?) =
            apply { this.notifyMobileOfPostLikes = notifyMobileOfPostLikes ?: false }

        fun notifyMobileOfUpdates(notifyMobileOfUpdates: Boolean?) =
            apply { this.notifyMobileOfUpdates = notifyMobileOfUpdates ?: false }

        fun notifyMobileOfMarketingUpdate(notifyMobileOfMarketingUpdate: Boolean?) =
            apply { this.notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate ?: false }

        fun notifyOfBackings(notifyOfBackings: Boolean?) =
            apply { this.notifyOfBackings = notifyOfBackings ?: false }

        fun notifyOfComments(notifyOfComments: Boolean?) =
            apply { this.notifyOfComments = notifyOfComments ?: false }

        fun notifyOfCommentReplies(notifyOfCommentReplies: Boolean?) =
            apply { this.notifyOfCommentReplies = notifyOfCommentReplies ?: false }

        fun notifyOfCreatorDigest(notifyOfCreatorDigest: Boolean?) =
            apply { this.notifyOfCreatorDigest = notifyOfCreatorDigest ?: false }

        fun notifyOfCreatorEdu(notifyOfCreatorEdu: Boolean?) =
            apply { this.notifyOfCreatorEdu = notifyOfCreatorEdu ?: false }

        fun notifyOfFollower(notifyOfFollower: Boolean?) =
            apply { this.notifyOfFollower = notifyOfFollower ?: false }

        fun notifyOfFriendActivity(notifyOfFriendActivity: Boolean?) =
            apply { this.notifyOfFriendActivity = notifyOfFriendActivity ?: false }

        fun notifyOfMessages(notifyOfMessages: Boolean?) =
            apply { this.notifyOfMessages = notifyOfMessages ?: false }

        fun notifyOfUpdates(notifyOfUpdates: Boolean?) =
            apply { this.notifyOfUpdates = notifyOfUpdates ?: false }

        fun showPublicProfile(showPublicProfile: Int?) =
            apply { this.showPublicProfile = showPublicProfile ?: 0 }

        fun social(social: Int?) = apply { this.social = social ?: 0 }
        fun alumniNewsletter(alumniNewsletter: Int?) =
            apply { this.alumniNewsletter = alumniNewsletter ?: 0 }

        fun artsCultureNewsletter(artsCultureNewsletter: Int?) =
            apply { this.artsCultureNewsletter = artsCultureNewsletter ?: 0 }

        fun filmNewsletter(filmNewsletter: Int?) =
            apply { this.filmNewsletter = filmNewsletter ?: 0 }

        fun gamesNewsletter(gamesNewsletter: Int?) =
            apply { this.gamesNewsletter = gamesNewsletter ?: 0 }

        fun happeningNewsletter(happeningNewsletter: Int?) =
            apply { this.happeningNewsletter = happeningNewsletter ?: 0 }

        fun inventNewsletter(inventNewsletter: Int?) =
            apply { this.inventNewsletter = inventNewsletter ?: 0 }

        fun musicNewsletter(musicNewsletter: Int?) =
            apply { this.musicNewsletter = musicNewsletter ?: 0 }

        fun promoNewsletter(promoNewsletter: Int?) =
            apply { this.promoNewsletter = promoNewsletter ?: 0 }

        fun publishingNewsletter(publishingNewsletter: Int?) =
            apply { this.publishingNewsletter = publishingNewsletter ?: 0 }

        fun weeklyNewsletter(weeklyNewsletter: Int?) =
            apply { this.weeklyNewsletter = weeklyNewsletter ?: 0 }

        fun build() = SettingsBody(
            optedOutOfRecommendations = optedOutOfRecommendations,
            notifyMobileOfBackings = notifyMobileOfBackings,
            notifyMobileOfComments = notifyMobileOfComments,
            notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu,
            notifyMobileOfFollower = notifyMobileOfFollower,
            notifyMobileOfFriendActivity = notifyMobileOfFriendActivity,
            notifyMobileOfMessages = notifyMobileOfMessages,
            notifyMobileOfPostLikes = notifyMobileOfPostLikes,
            notifyMobileOfUpdates = notifyMobileOfUpdates,
            notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate,
            notifyOfBackings = notifyOfBackings,
            notifyOfComments = notifyOfComments,
            notifyOfCommentReplies = notifyOfCommentReplies,
            notifyOfCreatorDigest = notifyOfCreatorDigest,
            notifyOfCreatorEdu = notifyOfCreatorEdu,
            notifyOfFollower = notifyOfFollower,
            notifyOfFriendActivity = notifyOfFriendActivity,
            notifyOfMessages = notifyOfMessages,
            notifyOfUpdates = notifyOfUpdates,
            showPublicProfile = showPublicProfile,
            social = social,
            alumniNewsletter = alumniNewsletter,
            artsCultureNewsletter = artsCultureNewsletter,
            filmNewsletter = filmNewsletter,
            gamesNewsletter = gamesNewsletter,
            happeningNewsletter = happeningNewsletter,
            inventNewsletter = inventNewsletter,
            musicNewsletter = musicNewsletter,
            promoNewsletter = promoNewsletter,
            publishingNewsletter = publishingNewsletter,
            weeklyNewsletter = weeklyNewsletter
        )
    }

    fun toBuilder() = Builder(
        optedOutOfRecommendations = optedOutOfRecommendations,
        notifyMobileOfBackings = notifyMobileOfBackings,
        notifyMobileOfComments = notifyMobileOfComments,
        notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu,
        notifyMobileOfFollower = notifyMobileOfFollower,
        notifyMobileOfFriendActivity = notifyMobileOfFriendActivity,
        notifyMobileOfMessages = notifyMobileOfMessages,
        notifyMobileOfPostLikes = notifyMobileOfPostLikes,
        notifyMobileOfUpdates = notifyMobileOfUpdates,
        notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate,
        notifyOfBackings = notifyOfBackings,
        notifyOfComments = notifyOfComments,
        notifyOfCommentReplies = notifyOfCommentReplies,
        notifyOfCreatorDigest = notifyOfCreatorDigest,
        notifyOfCreatorEdu = notifyOfCreatorEdu,
        notifyOfFollower = notifyOfFollower,
        notifyOfFriendActivity = notifyOfFriendActivity,
        notifyOfMessages = notifyOfMessages,
        notifyOfUpdates = notifyOfUpdates,
        showPublicProfile = showPublicProfile,
        social = social,
        alumniNewsletter = alumniNewsletter,
        artsCultureNewsletter = artsCultureNewsletter,
        filmNewsletter = filmNewsletter,
        gamesNewsletter = gamesNewsletter,
        happeningNewsletter = happeningNewsletter,
        inventNewsletter = inventNewsletter,
        musicNewsletter = musicNewsletter,
        promoNewsletter = promoNewsletter,
        publishingNewsletter = publishingNewsletter,
        weeklyNewsletter = weeklyNewsletter
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is SettingsBody) {
            equals = optedOutOfRecommendations() == other.optedOutOfRecommendations() &&
                notifyMobileOfBackings() == other.notifyMobileOfBackings() &&
                notifyMobileOfComments() == other.notifyMobileOfComments() &&
                notifyMobileOfCreatorEdu() == other.notifyMobileOfCreatorEdu() &&
                notifyMobileOfFollower() == other.notifyMobileOfFollower() &&
                notifyMobileOfFriendActivity() == other.notifyMobileOfFriendActivity() &&
                notifyMobileOfMessages() == other.notifyMobileOfMessages() &&
                notifyMobileOfPostLikes() == other.notifyMobileOfPostLikes() &&
                notifyMobileOfUpdates() == other.notifyMobileOfUpdates() &&
                notifyMobileOfMarketingUpdate() == other.notifyMobileOfMarketingUpdate() &&
                notifyOfBackings() == other.notifyOfBackings() &&
                notifyOfComments() == other.notifyOfComments() &&
                notifyOfCommentReplies() == other.notifyOfCommentReplies() &&
                notifyOfCreatorDigest() == other.notifyOfCreatorDigest() &&
                notifyOfCreatorEdu() == other.notifyOfCreatorEdu() &&
                notifyOfFollower() == other.notifyOfFollower() &&
                notifyOfFriendActivity() == other.notifyOfFriendActivity() &&
                notifyOfMessages() == other.notifyOfMessages() &&
                notifyOfUpdates() == other.notifyOfUpdates() &&
                showPublicProfile() == other.showPublicProfile() &&
                social() == other.social() &&
                alumniNewsletter() == other.alumniNewsletter() &&
                artsCultureNewsletter() == other.artsCultureNewsletter() &&
                filmNewsletter() == other.filmNewsletter() &&
                gamesNewsletter() == other.gamesNewsletter() &&
                happeningNewsletter() == other.happeningNewsletter() &&
                inventNewsletter() == other.inventNewsletter() &&
                musicNewsletter() == other.musicNewsletter() &&
                promoNewsletter() == other.promoNewsletter() &&
                publishingNewsletter() == other.publishingNewsletter() &&
                weeklyNewsletter() == other.weeklyNewsletter()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
