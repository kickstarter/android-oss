package com.kickstarter.models

import android.content.res.Resources
import android.os.Parcelable
import com.kickstarter.R
import kotlinx.parcelize.Parcelize

@Parcelize
class User private constructor(
    private val alumniNewsletter: Boolean,
    private val artsCultureNewsletter: Boolean,
    private val avatar: Avatar,
    private val backedProjectsCount: Int,
    private val createdProjectsCount: Int,
    private val draftProjectsCount: Int,
    private val erroredBackingsCount: Int,
    private val facebookConnected: Boolean,
    private val filmNewsletter: Boolean,
    private val gamesNewsletter: Boolean,
    private val happeningNewsletter: Boolean,
    private val id: Long,
    private val inventNewsletter: Boolean,
    private val isAdmin: Boolean,
    private val isEmailVerified: Boolean,
    private val email: String?,
    private val hasPassword: Boolean?,
    private val isCreator: Boolean?,
    private val isDeliverable: Boolean?,
    private val chosenCurrency: String?,
    private val location: Location?,
    private val memberProjectsCount: Int,
    private val musicNewsletter: Boolean,
    private val name: String,
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
    private val optedOutOfRecommendations: Boolean,
    private val ppoHasAction: Boolean,
    private val promoNewsletter: Boolean,
    private val publishingNewsletter: Boolean,
    private val showPublicProfile: Boolean,
    private val needsPassword: Boolean?,
    private val social: Boolean,
    private val starredProjectsCount: Int,
    private val unreadMessagesCount: Int,
    private val unseenActivityCount: Int,
    private val weeklyNewsletter: Boolean
) : Parcelable, Relay {

    fun alumniNewsletter() = this.alumniNewsletter
    fun artsCultureNewsletter() = this.artsCultureNewsletter
    fun avatar() = this.avatar
    fun backedProjectsCount() = this.backedProjectsCount
    fun createdProjectsCount() = this.createdProjectsCount
    fun draftProjectsCount() = this.draftProjectsCount
    fun erroredBackingsCount() = this.erroredBackingsCount
    fun facebookConnected() = this.facebookConnected
    fun filmNewsletter() = this.filmNewsletter
    fun gamesNewsletter() = this.gamesNewsletter
    fun happeningNewsletter() = this.happeningNewsletter
    override fun id() = this.id
    fun isAdmin() = this.isAdmin
    fun hasPassword() = this.hasPassword

    fun isEmailVerified() = this.isEmailVerified
    fun isCreator() = this.isCreator
    fun isDeliverable() = this.isDeliverable
    fun email() = this.email
    fun chosenCurrency() = this.chosenCurrency
    fun inventNewsletter() = this.inventNewsletter
    fun location() = this.location
    fun memberProjectsCount() = this.memberProjectsCount
    fun musicNewsletter() = this.musicNewsletter
    fun name() = this.name
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
    fun optedOutOfRecommendations() = this.optedOutOfRecommendations
    fun ppoHasAction() = this.ppoHasAction
    fun promoNewsletter() = this.promoNewsletter
    fun publishingNewsletter() = this.publishingNewsletter
    fun showPublicProfile() = this.showPublicProfile
    fun needsPassword() = this.needsPassword
    fun social() = this.social
    fun starredProjectsCount() = this.starredProjectsCount
    fun unreadMessagesCount() = this.unreadMessagesCount
    fun unseenActivityCount() = this.unseenActivityCount
    fun weeklyNewsletter() = this.weeklyNewsletter

    @Parcelize
    data class Builder(
        private var alumniNewsletter: Boolean = false,
        private var artsCultureNewsletter: Boolean = false,
        private var avatar: Avatar = Avatar.builder().build(),
        private var backedProjectsCount: Int = 0,
        private var createdProjectsCount: Int = 0,
        private var draftProjectsCount: Int = 0,
        private var erroredBackingsCount: Int = 0,
        private var facebookConnected: Boolean = false,
        private var filmNewsletter: Boolean = false,
        private var gamesNewsletter: Boolean = false,
        private var happeningNewsletter: Boolean = false,
        private var id: Long = 0L,
        private var inventNewsletter: Boolean = false,
        private var isAdmin: Boolean = false,
        private var isEmailVerified: Boolean = false,
        private var isCreator: Boolean? = false,
        private var isDeliverable: Boolean? = false,
        private var email: String? = null,
        private var chosenCurrency: String? = null,
        private var location: Location? = null,
        private var memberProjectsCount: Int = 0,
        private var musicNewsletter: Boolean = false,
        private var name: String = "",
        private var notifyMobileOfBackings: Boolean = false,
        private var hasPassword: Boolean? = false,
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
        private var optedOutOfRecommendations: Boolean = false,
        private var promoNewsletter: Boolean = false,
        private var ppoHasAction: Boolean = false,
        private var publishingNewsletter: Boolean = false,
        private var showPublicProfile: Boolean = false,
        private var needsPassword: Boolean? = false,
        private var social: Boolean = false,
        private var starredProjectsCount: Int = 0,
        private var unreadMessagesCount: Int = 0,
        private var unseenActivityCount: Int = 0,
        private var weeklyNewsletter: Boolean = false
    ) : Parcelable {

        fun alumniNewsletter(alN: Boolean?) = apply { this.alumniNewsletter = alN ?: false }
        fun artsCultureNewsletter(arN: Boolean?) = apply { this.artsCultureNewsletter = arN ?: false }
        fun avatar(avatar: Avatar?) = apply { avatar?.let { this.avatar = it } }
        fun backedProjectsCount(bPC: Int?) = apply { this.backedProjectsCount = bPC ?: 0 }
        fun draftProjectsCount(dPC: Int?) = apply { this.draftProjectsCount = dPC ?: 0 }
        fun createdProjectsCount(cPC: Int?) = apply { this.createdProjectsCount = cPC ?: 0 }
        fun erroredBackingsCount(eBC: Int?) = apply { this.erroredBackingsCount = eBC ?: 0 }
        fun facebookConnected(facebookConnected: Boolean?) = apply { this.facebookConnected = facebookConnected ?: false }
        fun filmNewsletter(filmNewsletter: Boolean?) = apply { this.filmNewsletter = filmNewsletter ?: false }
        fun gamesNewsletter(gamesNewsletter: Boolean?) = apply { this.gamesNewsletter = gamesNewsletter ?: false }
        fun happeningNewsletter(happeningNewsletter: Boolean?) = apply { this.happeningNewsletter = happeningNewsletter ?: false }
        fun id(id: Long?) = apply { this.id = id ?: 0 }
        fun isAdmin(isAdmin: Boolean?) = apply { this.isAdmin = isAdmin ?: false }
        fun isEmailVerified(isEmailVerified: Boolean?) = apply { this.isEmailVerified = isEmailVerified ?: false }
        fun isCreator(isCreator: Boolean?) = apply { this.isCreator = isCreator ?: false }
        fun isDeliverable(isDeliverable: Boolean?) = apply { this.isDeliverable = isDeliverable ?: false }
        fun email(email: String?) = apply { this.email = email }
        fun chosenCurrency(chosenCurrency: String?) = apply { this.chosenCurrency = chosenCurrency }
        fun inventNewsletter(inventNewsletter: Boolean?) = apply { this.inventNewsletter = inventNewsletter ?: false }
        fun hasPassword(hasPassword: Boolean?) = apply { this.hasPassword = hasPassword ?: false }
        fun location(location: Location?) = apply { this.location = location }
        fun memberProjectsCount(memberProjectsCount: Int?) = apply { this.memberProjectsCount = memberProjectsCount ?: 0 }
        fun musicNewsletter(musicNewsletter: Boolean?) = apply { this.musicNewsletter = musicNewsletter ?: false }
        fun name(name: String?) = apply { name?.let { this.name = it } }
        fun notifyMobileOfBackings(notifyOfFriendActivity: Boolean?) = apply { this.notifyMobileOfBackings = notifyOfFriendActivity ?: false }
        fun notifyMobileOfComments(notifyMobileOfComments: Boolean?) = apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfCreatorEdu(notifyMobileOfCreatorEdu: Boolean?) = apply { this.notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu ?: false }
        fun notifyMobileOfFollower(notifyMobileOfFollower: Boolean?) = apply { this.notifyMobileOfFollower = notifyMobileOfFollower ?: false }
        fun notifyMobileOfFriendActivity(notifyMobileOfFriendActivity: Boolean?) = apply { this.notifyMobileOfFriendActivity = notifyMobileOfFriendActivity ?: false }
        fun notifyMobileOfMessages(notifyMobileOfMessages: Boolean?) = apply { this.notifyMobileOfMessages = notifyMobileOfMessages ?: false }
        fun notifyMobileOfPostLikes(notifyMobileOfPostLikes: Boolean?) = apply { this.notifyMobileOfPostLikes = notifyMobileOfPostLikes ?: false }
        fun notifyMobileOfUpdates(notifyMobileOfUpdates: Boolean?) = apply { this.notifyMobileOfUpdates = notifyMobileOfUpdates ?: false }
        fun notifyMobileOfMarketingUpdate(notifyMobileOfMarketingUpdate: Boolean?) = apply { this.notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate ?: false }
        fun notifyOfBackings(notifyOfBackings: Boolean?) = apply { this.notifyOfBackings = notifyOfBackings ?: false }
        fun notifyOfComments(notifyOfComments: Boolean?) = apply { this.notifyOfComments = notifyOfComments ?: false }
        fun notifyOfCommentReplies(notifyOfCommentReplies: Boolean?) = apply { this.notifyOfCommentReplies = notifyOfCommentReplies ?: false }
        fun notifyOfCreatorDigest(notifyOfCreatorDigest: Boolean?) = apply { this.notifyOfCreatorDigest = notifyOfCreatorDigest ?: false }
        fun notifyOfCreatorEdu(notifyOfCreatorEdu: Boolean?) = apply { this.notifyOfCreatorEdu = notifyOfCreatorEdu ?: false }
        fun notifyOfFollower(notifyOfFollower: Boolean?) = apply { this.notifyOfFollower = notifyOfFollower ?: false }
        fun notifyOfFriendActivity(notifyOfFriendActivity: Boolean?) = apply { this.notifyOfFriendActivity = notifyOfFriendActivity ?: false }
        fun notifyOfMessages(notifyOfMessages: Boolean?) = apply { this.notifyOfMessages = notifyOfMessages ?: false }
        fun notifyOfUpdates(notifyOfUpdates: Boolean?) = apply { this.notifyOfUpdates = notifyOfUpdates ?: false }
        fun optedOutOfRecommendations(optedOutOfRecommendations: Boolean?) = apply { this.optedOutOfRecommendations = optedOutOfRecommendations ?: false }
        fun ppoHasAction(ppoHasAction: Boolean?) = apply { this.ppoHasAction = ppoHasAction ?: false }
        fun promoNewsletter(promoNewsletter: Boolean?) = apply { this.promoNewsletter = promoNewsletter ?: false }
        fun publishingNewsletter(publishingNewsletter: Boolean?) = apply { this.publishingNewsletter = publishingNewsletter ?: false }
        fun showPublicProfile(showPublicProfile: Boolean?) = apply { this.showPublicProfile = showPublicProfile ?: false }
        fun social(social: Boolean?) = apply { this.social = social ?: false }
        fun starredProjectsCount(starredProjectsCount: Int?) = apply { starredProjectsCount?.let { this.starredProjectsCount = it } }
        fun unreadMessagesCount(unreadMessagesCount: Int?) = apply { this.unreadMessagesCount = unreadMessagesCount ?: 0 }
        fun unseenActivityCount(unseenActivityCount: Int?) = apply { this.unseenActivityCount = unseenActivityCount ?: 0 }
        fun weeklyNewsletter(weeklyNewsletter: Boolean?) = apply { this.weeklyNewsletter = weeklyNewsletter ?: false }
        fun needsPassword(needsPassword: Boolean?) = apply { this.needsPassword = needsPassword ?: false }

        fun build() = User(
            alumniNewsletter = alumniNewsletter,
            artsCultureNewsletter = artsCultureNewsletter,
            avatar = avatar,
            backedProjectsCount = backedProjectsCount,
            createdProjectsCount = createdProjectsCount,
            draftProjectsCount = draftProjectsCount,
            erroredBackingsCount = erroredBackingsCount,
            facebookConnected = facebookConnected,
            filmNewsletter = filmNewsletter,
            gamesNewsletter = gamesNewsletter,
            happeningNewsletter = happeningNewsletter,
            id = id,
            hasPassword = hasPassword,
            inventNewsletter = inventNewsletter,
            isAdmin = isAdmin,
            isEmailVerified = isEmailVerified,
            isCreator = isCreator,
            isDeliverable = isDeliverable,
            email = email,
            chosenCurrency = chosenCurrency,
            location = location,
            memberProjectsCount = memberProjectsCount,
            musicNewsletter = musicNewsletter,
            name = name,
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
            optedOutOfRecommendations = optedOutOfRecommendations,
            ppoHasAction = ppoHasAction,
            promoNewsletter = promoNewsletter,
            publishingNewsletter = publishingNewsletter,
            showPublicProfile = showPublicProfile,
            social = social,
            starredProjectsCount = starredProjectsCount,
            unreadMessagesCount = unreadMessagesCount,
            unseenActivityCount = unseenActivityCount,
            weeklyNewsletter = weeklyNewsletter,
            needsPassword = needsPassword
        )
    }

    fun param(): String {
        return id().toString()
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        alumniNewsletter = alumniNewsletter,
        artsCultureNewsletter = artsCultureNewsletter,
        avatar = avatar,
        backedProjectsCount = backedProjectsCount,
        createdProjectsCount = createdProjectsCount,
        draftProjectsCount = draftProjectsCount,
        erroredBackingsCount = erroredBackingsCount,
        facebookConnected = facebookConnected,
        filmNewsletter = filmNewsletter,
        gamesNewsletter = gamesNewsletter,
        happeningNewsletter = happeningNewsletter,
        id = id,
        inventNewsletter = inventNewsletter,
        isAdmin = isAdmin,
        isEmailVerified = isEmailVerified,
        email = email,
        hasPassword = hasPassword,
        isCreator = isCreator,
        isDeliverable = isDeliverable,
        chosenCurrency = chosenCurrency,
        location = location,
        memberProjectsCount = memberProjectsCount,
        musicNewsletter = musicNewsletter,
        name = name,
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
        optedOutOfRecommendations = optedOutOfRecommendations,
        ppoHasAction = ppoHasAction,
        promoNewsletter = promoNewsletter,
        publishingNewsletter = publishingNewsletter,
        showPublicProfile = showPublicProfile,
        social = social,
        starredProjectsCount = starredProjectsCount,
        unreadMessagesCount = unreadMessagesCount,
        unseenActivityCount = unseenActivityCount,
        weeklyNewsletter = weeklyNewsletter,
        needsPassword = needsPassword
    )

    enum class EmailFrequency(private val stringResId: Int) {
        TWICE_A_DAY_SUMMARY(R.string.Twice_a_day_summary), DAILY_SUMMARY(R.string.Daily_summary);

        companion object {
            fun getStrings(resources: Resources): Array<String?> {
                val strings = arrayOfNulls<String>(values().size)
                val values = values()
                var i = 0
                val valuesLength = values.size
                while (i < valuesLength) {
                    val emailFrequency = values[i]
                    strings[i] = resources.getString(emailFrequency.stringResId)
                    i++
                }
                return strings
            }
        }
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is User) {
            equals = id() == obj.id() &&
                alumniNewsletter() == obj.alumniNewsletter() &&
                artsCultureNewsletter() == obj.artsCultureNewsletter() &&
                backedProjectsCount() == obj.backedProjectsCount() &&
                createdProjectsCount() == obj.createdProjectsCount() &&
                draftProjectsCount() == obj.draftProjectsCount() &&
                name() == obj.name() &&
                avatar() == obj.avatar() &&
                createdProjectsCount() == obj.createdProjectsCount() &&
                facebookConnected() == obj.facebookConnected() &&
                filmNewsletter() == obj.filmNewsletter() &&
                facebookConnected() == obj.facebookConnected() &&
                gamesNewsletter() == obj.gamesNewsletter() &&
                happeningNewsletter() == obj.happeningNewsletter() &&
                inventNewsletter() == obj.inventNewsletter() &&
                isAdmin() == obj.isAdmin() &&
                location() == obj.location() &&
                memberProjectsCount() == obj.memberProjectsCount() &&
                musicNewsletter() == obj.musicNewsletter() &&
                notifyMobileOfBackings() == obj.notifyMobileOfBackings() &&
                notifyMobileOfComments() == obj.notifyMobileOfComments() &&
                notifyMobileOfCreatorEdu() == obj.notifyMobileOfCreatorEdu() &&
                notifyMobileOfFollower() == obj.notifyMobileOfFollower() &&
                notifyMobileOfFriendActivity() == obj.notifyMobileOfFriendActivity() &&
                notifyMobileOfMessages() == obj.notifyMobileOfMessages() &&
                notifyMobileOfPostLikes() == obj.notifyMobileOfPostLikes() &&
                notifyMobileOfUpdates() == obj.notifyMobileOfUpdates() &&
                notifyMobileOfMarketingUpdate() == obj.notifyMobileOfMarketingUpdate() &&
                notifyOfBackings() == obj.notifyOfBackings() &&
                notifyOfComments() == obj.notifyOfComments() &&
                notifyOfCommentReplies() == obj.notifyOfCommentReplies() &&
                notifyOfCreatorDigest() == obj.notifyOfCreatorDigest() &&
                notifyOfCreatorEdu() == obj.notifyOfCreatorEdu() &&
                notifyOfFollower() == obj.notifyOfFollower() &&
                notifyOfFriendActivity() == obj.notifyOfFriendActivity() &&
                notifyOfMessages() == obj.notifyOfMessages() &&
                optedOutOfRecommendations() == obj.optedOutOfRecommendations() &&
                ppoHasAction() == obj.ppoHasAction() &&
                promoNewsletter() == obj.promoNewsletter() &&
                publishingNewsletter() == obj.publishingNewsletter() &&
                showPublicProfile() == obj.showPublicProfile() &&
                social() == obj.social() &&
                starredProjectsCount() == obj.starredProjectsCount() &&
                unreadMessagesCount() == obj.unreadMessagesCount() &&
                unseenActivityCount() == obj.unseenActivityCount() &&
                weeklyNewsletter() == obj.weeklyNewsletter() &&
                needsPassword() == obj.needsPassword() &&
                email() == obj.email() &&
                hasPassword() == obj.hasPassword() &&
                isCreator() == obj.isCreator() &&
                isDeliverable() == obj.isDeliverable() &&
                isDeliverable() == obj.isDeliverable()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
