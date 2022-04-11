package com.kickstarter.services.apirequests

import android.os.Parcelable
import auto.parcel.AutoParcel

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
) :Parcelable {
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
        fun optedOutOfRecommendations(optedOutOfRecommendations: Int?) = apply { this.optedOutOfRecommendations = optedOutOfRecommendations ?: 0 }
        fun notifyMobileOfBackings(notifyMobileOfBackings: Boolean?) = apply { this.notifyMobileOfBackings = notifyMobileOfBackings ?: false }
        fun notifyMobileOfComments(notifyMobileOfComments: Boolean?) = apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfCreatorEdu(notifyMobileOfCreatorEdu: Boolean?) = apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfFollower(notifyMobileOfFollower: Boolean?) = apply { this.notifyMobileOfFollower = notifyMobileOfFollower ?: false }
        fun notifyMobileOfFriendActivity(__: Boolean): Builder? = apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfMessages(__: Boolean): Builder?= apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfPostLikes(__: Boolean): Builder?= apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfUpdates(__: Boolean): Builder?= apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfMarketingUpdate(__: Boolean): Builder?= apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyOfBackings(__: Boolean): Builder?= apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyOfComments(__: Boolean): Builder?= apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyOfCommentReplies(__: Boolean): Builder?
        fun notifyOfCreatorDigest(__: Boolean): Builder?
        fun notifyOfCreatorEdu(__: Boolean): Builder?
        fun notifyOfFollower(__: Boolean): Builder?
        fun notifyOfFriendActivity(__: Boolean): Builder?
        fun notifyOfMessages(__: Boolean): Builder?
        fun notifyOfUpdates(__: Boolean): Builder?
        fun showPublicProfile(__: Int): Builder?
        fun social(__: Int): Builder?
        fun alumniNewsletter(__: Int): Builder?
        fun artsCultureNewsletter(__: Int): Builder?
        fun filmNewsletter(__: Int): Builder?
        fun gamesNewsletter(__: Int): Builder?
        fun happeningNewsletter(__: Int): Builder?
        fun inventNewsletter(__: Int): Builder?
        fun musicNewsletter(__: Int): Builder?
        fun promoNewsletter(__: Int): Builder?
        fun publishingNewsletter(__: Int): Builder?
        fun weeklyNewsletter(__: Int): Builder?
        fun build(): SettingsBody?
    }

    abstract fun toBuilder(): Builder?

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_SettingsBody.Builder()
        }
    }
}