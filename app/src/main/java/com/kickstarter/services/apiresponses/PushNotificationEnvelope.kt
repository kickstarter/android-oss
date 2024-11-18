package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.SurveyResponse.Urls
import com.kickstarter.models.SurveyResponse.Urls.Web
import com.kickstarter.models.pushdata.Activity
import com.kickstarter.models.pushdata.GCM
import kotlinx.parcelize.Parcelize

@Parcelize
class PushNotificationEnvelope private constructor(
    private val activity: Activity?,
    private val erroredPledge: ErroredPledge?,
    private val gcm: GCM,
    private val message: Message?,
    private val project: Project?,
    private val survey: Survey?,
    private val pledgeRedemption: PledgeRedemption?,

) : Parcelable {
    fun activity() = this.activity
    fun erroredPledge() = this.erroredPledge
    fun gcm() = this.gcm
    fun message() = this.message
    fun project() = this.project
    fun survey() = this.survey
    fun pledgeRedemption() = this.pledgeRedemption

    @Parcelize
    data class Builder(
        private var activity: Activity? = null,
        private var erroredPledge: ErroredPledge? = null,
        private var gcm: GCM = GCM.builder().build(),
        private var message: Message? = null,
        private var project: Project? = null,
        private var survey: Survey? = null,
        private var pledgeRedemption: PledgeRedemption? = null
    ) : Parcelable {
        fun activity(activity: Activity?) = apply { this.activity = activity }
        fun erroredPledge(erroredPledge: ErroredPledge?) = apply { this.erroredPledge = erroredPledge }
        fun gcm(gcm: GCM) = apply { this.gcm = gcm }
        fun message(message: Message?) = apply { this.message = message }
        fun project(project: Project?) = apply { this.project = project }
        fun survey(survey: Survey?) = apply { this.survey = survey }
        fun pledgeRedemption(pledgeRedemption: PledgeRedemption?) = apply { this.pledgeRedemption = pledgeRedemption }
        fun build() = PushNotificationEnvelope(
            activity = activity,
            erroredPledge = erroredPledge,
            gcm = gcm,
            message = message,
            project = project,
            survey = survey,
            pledgeRedemption = pledgeRedemption,
        )
    }

    fun toBuilder() = Builder(
        activity = activity,
        erroredPledge = erroredPledge,
        gcm = gcm,
        message = message,
        project = project,
        survey = survey,
        pledgeRedemption = pledgeRedemption
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PushNotificationEnvelope) {
            equals = activity() == other.activity() &&
                erroredPledge() == other.erroredPledge() &&
                gcm() == other.gcm() &&
                message() == other.message() &&
                project() == other.project() &&
                pledgeRedemption() == other.pledgeRedemption() &&
                survey() == other.survey()
        }
        return equals
    }

    fun isErroredPledge() = erroredPledge() != null

    fun isFriendFollow() = activity() != null && activity()?.category() == com.kickstarter.models.Activity.CATEGORY_FOLLOW

    fun isMessage() = message() != null

    fun isProjectActivity() = if (activity() != null) {
        PROJECT_NOTIFICATION_CATEGORIES.contains(
            activity()?.category()
        )
    } else false

    fun isProjectReminder() = project() != null

    fun isProjectUpdateActivity() = activity() != null && activity()?.category() == com.kickstarter.models.Activity.CATEGORY_UPDATE

    fun isSurvey() = survey() != null

    fun isPledgeRedemption() = pledgeRedemption() != null

    fun signature(): Int {
        // When we display an Android notification, we can give it a id. If the server sends a notification with the same
        // id, Android updates the existing notification with new information rather than creating a new notification.
        //
        // The server doesn't send unique notification ids, so hashing the alert text is a weak substitute. Probably won't
        // make use of this feature anyhow.
        return gcm().alert().hashCode()
    }

    @Parcelize
    class ErroredPledge private constructor(private val projectId: Long) : Parcelable {
        fun projectId() = this.projectId

        @Parcelize
        data class Builder(private var projectId: Long = 0L) : Parcelable {
            fun projectId(projectId: Long) = apply { this.projectId = projectId }
            fun build() = ErroredPledge(projectId = projectId)
        }

        fun toBuilder() = Builder(
            projectId = projectId
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is ErroredPledge) {
                equals = projectId() == other.projectId()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class Message private constructor(
        private val messageThreadId: Long,
        private val projectId: Long
    ) : Parcelable {
        fun messageThreadId() = this.messageThreadId
        fun projectId() = this.projectId
        @Parcelize
        data class Builder(
            private var messageThreadId: Long = 0L,
            private var projectId: Long = 0L,
        ) : Parcelable {
            fun messageThreadId(messageThreadId: Long) = apply { this.messageThreadId = messageThreadId }
            fun projectId(projectId: Long) = apply { this.projectId = projectId }
            fun build() = Message(
                messageThreadId = messageThreadId,
                projectId = projectId
            )
        }

        fun toBuilder() = Builder(
            messageThreadId = messageThreadId,
            projectId = projectId
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is Message) {
                equals = messageThreadId() == other.messageThreadId() &&
                    projectId() == other.projectId()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class Project private constructor(
        private val id: Long,
        private val photo: String
    ) : Parcelable {
        fun id() = this.id
        fun photo() = this.photo
        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            private var photo: String = "",
        ) : Parcelable {
            fun id(id: Long) = apply { this.id = id }
            fun photo(photo: String) = apply { this.photo = photo }
            fun build() = Project(
                id = id,
                photo = photo
            )
        }

        fun toBuilder() = Builder(
            id = id,
            photo = photo
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is Project) {
                equals = id() == other.id() &&
                    photo() == other.photo()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class Survey private constructor(
        private val id: Long,
        private val projectId: Long,
        private val urls: Urls?
    ) : Parcelable {
        fun id() = this.id
        fun projectId() = this.projectId
        fun urls() = this.urls

        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            private var projectId: Long = 0L,
            private var urls: Urls? = null

        ) : Parcelable {
            fun id(id: Long) = apply { this.id = id }
            fun projectId(projectId: Long) = apply { this.projectId = projectId }
            fun urls(urls: Urls?) = apply { urls?.let { this.urls = it } }
            fun build() = Survey(
                id = id,
                projectId = projectId,
                urls = urls
            )
        }

        fun toBuilder() = Builder(
            id = id,
            projectId = projectId
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is Survey) {
                equals = id() == other.id() &&
                    projectId() == other.projectId()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class PledgeRedemption private constructor(
        private val id: Long,
        private val pledgeRedemptionPath: String?
    ) : Parcelable {
        fun id() = this.id
        fun pledgeRedemptionPath() = this.pledgeRedemptionPath

        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            private var pledgeRedemptionPath: String? = null

        ) : Parcelable {
            fun id(id: Long) = apply { this.id = id }
            fun pledgeRedemptionPath(pledgeRedemptionPath: String?) = apply { pledgeRedemptionPath?.let { this.pledgeRedemptionPath = it } }
            fun build() = PledgeRedemption(
                id = id,
                pledgeRedemptionPath = pledgeRedemptionPath
            )
        }

        fun toBuilder() = Builder(
            id = id,
            pledgeRedemptionPath = pledgeRedemptionPath
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is PledgeRedemption) {
                equals = id() == other.id() &&
                    pledgeRedemptionPath() == other.pledgeRedemptionPath()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class Urls private constructor(
        private val web: Web
    ) : Parcelable {
        fun web() = this.web

        @Parcelize
        data class Builder(
            private var web: Web = Web.builder()
                .build()
        ) : Parcelable {
            fun web(web: Web?) = apply { this.web = web ?: Web.builder().build() }

            fun build() = Urls(
                web = web
            )
        }

        fun toBuilder() = Builder(
            web = web,
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is Urls) {
                equals = web() == obj.web()
            }
            return equals
        }
    }

    @Parcelize
    class Web private constructor(
        private val survey: String?
    ) : Parcelable {
        fun survey() = this.survey

        @Parcelize
        data class Builder(
            private var survey: String? = null
        ) : Parcelable {
            fun survey(survey: String?) = apply { this.survey = survey }
            fun build() = Web(
                survey = survey
            )
        }

        fun toBuilder() = Builder(
            survey = survey
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is Web) {
                equals = survey() == obj.survey()
            }
            return equals
        }
    }

    companion object {
        private val PROJECT_NOTIFICATION_CATEGORIES = listOf(
            com.kickstarter.models.Activity.CATEGORY_BACKING,
            com.kickstarter.models.Activity.CATEGORY_CANCELLATION,
            com.kickstarter.models.Activity.CATEGORY_FAILURE,
            com.kickstarter.models.Activity.CATEGORY_LAUNCH,
            com.kickstarter.models.Activity.CATEGORY_SUCCESS
        )
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
