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
        private val optedOutOfRecommendations: Int = 0,
        private val notifyMobileOfBackings: Boolean = false,
        private val notifyMobileOfComments: Boolean = false,
        private val notifyMobileOfCreatorEdu: Boolean = false,
        private val notifyMobileOfFollower: Boolean = false,
        private val notifyMobileOfFriendActivity: Boolean = false,
        private val notifyMobileOfMessages: Boolean = false,
        private val notifyMobileOfPostLikes: Boolean = false,
        private val notifyMobileOfUpdates: Boolean = false,
        private val notifyMobileOfMarketingUpdate: Boolean = false,
        private val notifyOfBackings: Boolean = false,
        private val notifyOfComments: Boolean = false,
        private val notifyOfCommentReplies: Boolean = false,
        private val notifyOfCreatorDigest: Boolean = false,
        private val notifyOfCreatorEdu: Boolean = false,
        private val notifyOfFollower: Boolean = false,
        private val notifyOfFriendActivity: Boolean = false,
        private val notifyOfMessages: Boolean = false,
        private val notifyOfUpdates: Boolean = false,
        private val showPublicProfile: Int = 0,
        private val social: Int = 0,
        private val alumniNewsletter: Int = 0,
        private val artsCultureNewsletter: Int = 0,
        private val filmNewsletter: Int = 0,
        private val gamesNewsletter: Int = 0,
        private val happeningNewsletter: Int = 0,
        private val inventNewsletter: Int = 0,
        private val musicNewsletter: Int = 0,
        private val promoNewsletter: Int = 0,
        private val publishingNewsletter: Int = 0,
        private val weeklyNewsletter: Int = 0
    ) : Parcelable {
        fun optedOutOfRecommendations(optedOutOfRecommendations: Int): Builder?
        fun notifyMobileOfBackings(__: Boolean): Builder?
        fun notifyMobileOfComments(__: Boolean): Builder?
        fun notifyMobileOfCreatorEdu(__: Boolean): Builder?
        fun notifyMobileOfFollower(__: Boolean): Builder?
        fun notifyMobileOfFriendActivity(__: Boolean): Builder?
        fun notifyMobileOfMessages(__: Boolean): Builder?
        fun notifyMobileOfPostLikes(__: Boolean): Builder?
        fun notifyMobileOfUpdates(__: Boolean): Builder?
        fun notifyMobileOfMarketingUpdate(__: Boolean): Builder?
        fun notifyOfBackings(__: Boolean): Builder?
        fun notifyOfComments(__: Boolean): Builder?
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