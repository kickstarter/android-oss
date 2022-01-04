package com.kickstarter.models

import android.content.res.Resources
import com.kickstarter.libs.qualifiers.AutoGson
import auto.parcel.AutoParcel
import android.os.Parcelable
import com.kickstarter.R

@AutoGson
@AutoParcel
abstract class User : Parcelable, Relay {
    abstract fun alumniNewsletter(): Boolean?
    abstract fun artsCultureNewsletter(): Boolean?
    abstract fun avatar(): Avatar?
    abstract fun backedProjectsCount(): Int?
    abstract fun createdProjectsCount(): Int?
    abstract fun draftProjectsCount(): Int?
    abstract fun erroredBackingsCount(): Int?
    abstract fun facebookConnected(): Boolean?
    abstract fun filmNewsletter(): Boolean?
    abstract fun gamesNewsletter(): Boolean?
    abstract fun happeningNewsletter(): Boolean?
    abstract override fun id(): Long
    abstract fun inventNewsletter(): Boolean?
    abstract val isAdmin: Boolean?
    abstract val isEmailVerified: Boolean?
    abstract fun chosenCurrency(): String?
    abstract fun location(): Location?
    abstract fun memberProjectsCount(): Int?
    abstract fun musicNewsletter(): Boolean?
    abstract fun name(): String?
    abstract fun notifyMobileOfBackings(): Boolean?
    abstract fun notifyMobileOfComments(): Boolean?
    abstract fun notifyMobileOfCreatorEdu(): Boolean?
    abstract fun notifyMobileOfFollower(): Boolean?
    abstract fun notifyMobileOfFriendActivity(): Boolean?
    abstract fun notifyMobileOfMessages(): Boolean?
    abstract fun notifyMobileOfPostLikes(): Boolean?
    abstract fun notifyMobileOfUpdates(): Boolean?
    abstract fun notifyMobileOfMarketingUpdate(): Boolean?
    abstract fun notifyOfBackings(): Boolean?
    abstract fun notifyOfComments(): Boolean?
    abstract fun notifyOfCommentReplies(): Boolean?
    abstract fun notifyOfCreatorDigest(): Boolean?
    abstract fun notifyOfCreatorEdu(): Boolean?
    abstract fun notifyOfFollower(): Boolean?
    abstract fun notifyOfFriendActivity(): Boolean?
    abstract fun notifyOfMessages(): Boolean?
    abstract fun notifyOfUpdates(): Boolean?
    abstract fun optedOutOfRecommendations(): Boolean?
    abstract fun promoNewsletter(): Boolean?
    abstract fun publishingNewsletter(): Boolean?
    abstract fun showPublicProfile(): Boolean?
    abstract fun social(): Boolean?
    abstract fun starredProjectsCount(): Int?
    abstract fun unreadMessagesCount(): Int?
    abstract fun unseenActivityCount(): Int?
    abstract fun weeklyNewsletter(): Boolean?

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun alumniNewsletter(__: Boolean?): Builder?
        abstract fun artsCultureNewsletter(__: Boolean?): Builder?
        abstract fun avatar(__: Avatar?): Builder?
        abstract fun backedProjectsCount(__: Int?): Builder?
        abstract fun createdProjectsCount(__: Int?): Builder?
        abstract fun draftProjectsCount(__: Int?): Builder?
        abstract fun erroredBackingsCount(__: Int?): Builder?
        abstract fun facebookConnected(__: Boolean?): Builder?
        abstract fun filmNewsletter(__: Boolean?): Builder?
        abstract fun gamesNewsletter(__: Boolean?): Builder?
        abstract fun happeningNewsletter(__: Boolean?): Builder?
        abstract fun id(__: Long): Builder?
        abstract fun isAdmin(__: Boolean?): Builder?
        abstract fun isEmailVerified(__: Boolean?): Builder?
        abstract fun chosenCurrency(__: String?): Builder?
        abstract fun inventNewsletter(__: Boolean?): Builder?
        abstract fun location(__: Location?): Builder?
        abstract fun memberProjectsCount(__: Int?): Builder?
        abstract fun musicNewsletter(__: Boolean?): Builder?
        abstract fun name(__: String?): Builder?
        abstract fun notifyMobileOfBackings(__: Boolean?): Builder?
        abstract fun notifyMobileOfComments(__: Boolean?): Builder?
        abstract fun notifyMobileOfCreatorEdu(__: Boolean?): Builder?
        abstract fun notifyMobileOfFollower(__: Boolean?): Builder?
        abstract fun notifyMobileOfFriendActivity(__: Boolean?): Builder?
        abstract fun notifyMobileOfMessages(__: Boolean?): Builder?
        abstract fun notifyMobileOfPostLikes(__: Boolean?): Builder?
        abstract fun notifyMobileOfUpdates(__: Boolean?): Builder?
        abstract fun notifyMobileOfMarketingUpdate(__: Boolean?): Builder?
        abstract fun notifyOfBackings(__: Boolean?): Builder?
        abstract fun notifyOfComments(__: Boolean?): Builder?
        abstract fun notifyOfCommentReplies(__: Boolean?): Builder?
        abstract fun notifyOfCreatorDigest(__: Boolean?): Builder?
        abstract fun notifyOfCreatorEdu(__: Boolean?): Builder?
        abstract fun notifyOfFollower(__: Boolean?): Builder?
        abstract fun notifyOfFriendActivity(__: Boolean?): Builder?
        abstract fun notifyOfMessages(__: Boolean?): Builder?
        abstract fun notifyOfUpdates(__: Boolean?): Builder?
        abstract fun optedOutOfRecommendations(__: Boolean?): Builder?
        abstract fun promoNewsletter(__: Boolean?): Builder?
        abstract fun publishingNewsletter(__: Boolean?): Builder?
        abstract fun showPublicProfile(__: Boolean?): Builder?
        abstract fun social(__: Boolean?): Builder?
        abstract fun starredProjectsCount(__: Int?): Builder?
        abstract fun unreadMessagesCount(__: Int?): Builder?
        abstract fun unseenActivityCount(__: Int?): Builder?
        abstract fun weeklyNewsletter(__: Boolean?): Builder?
        abstract fun build(): User?
    }

    fun param(): String {
        return id().toString()
    }

    abstract fun toBuilder(): Builder?
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
                    isAdmin == obj.isAdmin &&
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
                    promoNewsletter() == obj.promoNewsletter() &&
                    publishingNewsletter() == obj.publishingNewsletter() &&
                    showPublicProfile() == obj.showPublicProfile() &&
                    social() == obj.social() &&
                    starredProjectsCount() == obj.starredProjectsCount() &&
                    unreadMessagesCount() == obj.unreadMessagesCount() &&
                    unseenActivityCount() == obj.unseenActivityCount() &&
                    weeklyNewsletter() == obj.weeklyNewsletter()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_User.Builder()
        }
    }
}