package com.kickstarter.models

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringDef
import com.kickstarter.libs.Permission
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.isNonZero
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class Project private constructor(
    private val availableCardTypes: List<String>?,
    private val backersCount: Int,
    private val blurb: String,
    private val backing: Backing?,
    private val category: Category?,
    private val commentsCount: Int?,
    private val country: String, // e.g.: US
    private val createdAt: DateTime,
    private val creator: User,
    private val currency: String, // e.g.: USD
    private val currencySymbol: String, // e.g.: $
    private val currentCurrency: String?, // e.g.: User's Preferred currency USD
    private val currencyTrailingCode: Boolean,
    private val displayPrelaunch: Boolean?,
    private val featuredAt: DateTime?,
    private val friends: List<User>?,
    private val fxRate: Float,
    private val deadline: DateTime?,
    private val sendMetaCapiEvents: Boolean?,
    private val sendThirdPartyEvents: Boolean?,
    private val goal: Double,
    private val id: Long, // in the Kickstarter app, this is project.pid not project.id
    private val isBacking: Boolean,
    private val isPledgeOverTimeAllowed: Boolean?,
    private val isStarred: Boolean,
    private val lastUpdatePublishedAt: DateTime?,
    private val launchedAt: DateTime?,
    private val location: Location?,
    private val name: String,
    private val permissions: List<Permission?>?,
    private val pledged: Double,
    private val photo: Photo?,
    private val prelaunchActivated: Boolean?,
    private val tags: List<String>?,
    private val rewards: List<Reward>?,
    private val slug: String?,
    private val staffPick: Boolean?,
    private val canComment: Boolean?,
    @State
    private val state: String,
    private val stateChangedAt: DateTime?,
    private val staticUsdRate: Float,
    private val usdExchangeRate: Float,
    private val unreadMessagesCount: Int?,
    private val unseenActivityCount: Int?,
    private val updatesCount: Int?,
    private val updatedAt: DateTime?,
    private val urls: Urls,
    private val video: Video?,
    private val projectFaqs: List<ProjectFaq>?,
    private val envCommitments: List<EnvironmentalCommitment>?,
    private val aiDisclosure: AiDisclosure?,
    private val risks: String?,
    private val story: String?,
    private val isFlagged: Boolean?,
    private val watchesCount: Int,
    private val isInPostCampaignPledgingPhase: Boolean? = null,
    private val postCampaignPledgingEnabled: Boolean? = null
) : Parcelable, Relay {
    fun availableCardTypes() = this.availableCardTypes
    fun backersCount() = this.backersCount
    fun blurb() = this.blurb
    fun backing() = this.backing
    fun category() = this.category
    fun commentsCount() = this.commentsCount
    fun country() = this.country
    fun createdAt() = this.createdAt
    fun creator() = this.creator
    fun currency() = this.currency
    fun currencySymbol() = this.currencySymbol
    fun currentCurrency() = this.currentCurrency
    fun currencyTrailingCode() = this.currencyTrailingCode
    fun displayPrelaunch() = this.displayPrelaunch
    fun featuredAt() = this.featuredAt
    fun friends() = this.friends ?: emptyList()
    fun fxRate() = this.fxRate
    fun deadline() = this.deadline
    fun goal() = this.goal
    override fun id() = this.id
    fun isBacking() = this.isBacking
    fun isPledgeOverTimeAllowed() = this.isPledgeOverTimeAllowed
    fun isStarred() = this.isStarred
    fun lastUpdatePublishedAt() = this.lastUpdatePublishedAt
    fun launchedAt() = this.launchedAt
    fun location() = this.location
    fun name() = this.name
    fun permissions() = this.permissions
    fun pledged() = this.pledged
    fun photo() = this.photo
    fun prelaunchActivated() = this.prelaunchActivated
    fun sendMetaCapiEvents() = this.sendMetaCapiEvents
    fun sendThirdPartyEvents() = this.sendThirdPartyEvents
    fun tags() = this.tags
    fun rewards() = this.rewards
    fun slug() = this.slug
    fun staffPick() = this.staffPick
    fun canComment() = this.canComment
    fun state() = this.state
    fun stateChangedAt() = this.stateChangedAt
    fun staticUsdRate() = this.staticUsdRate
    fun usdExchangeRate() = this.usdExchangeRate
    fun unreadMessagesCount() = this.unreadMessagesCount ?: 0
    fun unseenActivityCount() = this.unseenActivityCount ?: 0
    fun updatesCount() = this.updatesCount
    fun updatedAt() = this.updatedAt
    fun urls() = this.urls
    fun video() = this.video
    fun projectFaqs() = this.projectFaqs
    fun envCommitments() = this.envCommitments
    fun aiDisclosure() = this.aiDisclosure
    fun risks() = this.risks
    fun story() = this.story
    fun isFlagged() = this.isFlagged
    fun watchesCount() = this.watchesCount
    fun isInPostCampaignPledgingPhase() = this.isInPostCampaignPledgingPhase
    fun postCampaignPledgingEnabled() = this.postCampaignPledgingEnabled

    @Parcelize
    data class Builder(
        private var availableCardTypes: List<String>? = null,
        private var backersCount: Int = 0,
        private var blurb: String = "",
        private var backing: Backing? = null,
        private var category: Category? = null,
        private var commentsCount: Int? = null,
        private var country: String = "",
        private var createdAt: DateTime = DateTime.now(),
        private var creator: User = User.builder().build(),
        private var currency: String = "",
        private var currencySymbol: String = "",
        private var currentCurrency: String? = null,
        private var currencyTrailingCode: Boolean = false,
        private var displayPrelaunch: Boolean? = null,
        private var featuredAt: DateTime? = null,
        private var friends: List<User>? = emptyList(),
        private var fxRate: Float = 0f,
        private var deadline: DateTime? = null,
        private var goal: Double = 0.0,
        private var id: Long = 0L,
        private var isBacking: Boolean = false,
        private var isPledgeOverTimeAllowed: Boolean? = null,
        private var isStarred: Boolean = false,
        private var lastUpdatePublishedAt: DateTime? = null,
        private var launchedAt: DateTime? = null,
        private var location: Location? = null,
        private var name: String = "",
        private var permissions: List<Permission?>? = null,
        private var pledged: Double = 0.0,
        private var photo: Photo? = null,
        private var prelaunchActivated: Boolean? = null,
        private var sendMetaCapiEvents: Boolean? = null,
        private var sendThirdPartyEvents: Boolean? = null,
        private var tags: List<String>? = emptyList(),
        private var rewards: List<Reward>? = emptyList(),
        private var slug: String? = null,
        private var staffPick: Boolean? = null,
        private var canComment: Boolean? = null,
        @State
        private var state: String = STATE_STARTED,
        private var stateChangedAt: DateTime? = null,
        private var staticUsdRate: Float = 0f,
        private var usdExchangeRate: Float = 0f,
        private var unreadMessagesCount: Int? = null,
        private var unseenActivityCount: Int? = null,
        private var updatesCount: Int? = null,
        private var updatedAt: DateTime? = null,
        private var urls: Urls = Urls.builder().build(),
        private var video: Video? = null,
        private var projectFaqs: List<ProjectFaq>? = emptyList(),
        private var envCommitments: List<EnvironmentalCommitment>? = emptyList(),
        private var aiDisclosure: AiDisclosure? = null,
        private var risks: String? = "",
        private var story: String? = "",
        private var isFlagged: Boolean? = null,
        private var watchesCount: Int = 0,
        private var isInPostCampaignPledgingPhase: Boolean? = null,
        private var postCampaignPledgingEnabled: Boolean? = null
    ) : Parcelable {
        fun availableCardTypes(availableCardTypes: List<String>?) = apply { this.availableCardTypes = availableCardTypes }
        fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount ?: 0 }
        fun watchesCount(watchesCount: Int?) = apply { this.watchesCount = watchesCount ?: 0 }
        fun blurb(blurb: String?) = apply { this.blurb = blurb ?: "" }
        fun backing(backing: Backing?) = apply { this.backing = backing }
        fun category(category: Category?) = apply { this.category = category }
        fun commentsCount(commentsCount: Int?) = apply { this.commentsCount = commentsCount }
        fun country(country: String?) = apply { this.country = country ?: "" }
        fun createdAt(createdAt: DateTime?) = apply { createdAt?.let { this.createdAt = it } }
        fun creator(creator: User?) = apply { creator?.let { this.creator = it } }
        fun currency(currency: String?) = apply { currency?.let { this.currency = it } }
        fun currencySymbol(currencySymbol: String?) = apply { currencySymbol?.let { this.currencySymbol = it } }
        fun currentCurrency(currency: String?) = apply { this.currentCurrency = currency }
        fun currencyTrailingCode(currencyTrailingCode: Boolean?) = apply { this.currencyTrailingCode = currencyTrailingCode ?: false }
        fun displayPrelaunch(displayPrelaunch: Boolean?) = apply { this.displayPrelaunch = displayPrelaunch }
        fun canComment(canComment: Boolean?) = apply { this.canComment = canComment ?: false }
        fun deadline(deadline: DateTime?) = apply { this.deadline = deadline }
        fun featuredAt(featuredAt: DateTime?) = apply { this.featuredAt = featuredAt }
        fun friends(friends: List<User>?) = apply { this.friends = friends ?: emptyList() }
        fun fxRate(fxRate: Float?) = apply { this.fxRate = fxRate ?: 0f }
        fun goal(goal: Double?) = apply { this.goal = goal ?: 0.0 }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun isBacking(isBacking: Boolean?) = apply { this.isBacking = isBacking ?: false }
        fun isPledgeOverTimeAllowed(isPledgeOverTimeAllowed: Boolean?) = apply { this.isPledgeOverTimeAllowed = isPledgeOverTimeAllowed }
        fun isStarred(isStarred: Boolean?) = apply { this.isStarred = isStarred ?: false }
        fun lastUpdatePublishedAt(lastUpdatePublishedAt: DateTime?) = apply { this.lastUpdatePublishedAt = lastUpdatePublishedAt }
        fun launchedAt(launchedAt: DateTime?) = apply { this.launchedAt = launchedAt }
        fun location(location: Location?) = apply { this.location = location }
        fun name(name: String?) = apply { this.name = name ?: "" }
        fun permissions(permissions: List<Permission?>?) = apply { this.permissions = permissions?.filterNotNull() ?: emptyList() }
        fun pledged(pledged: Double?) = apply { this.pledged = pledged ?: 0.0 }
        fun photo(photo: Photo?) = apply { this.photo = photo }
        fun prelaunchActivated(prelaunchActivated: Boolean?) = apply { this.prelaunchActivated = prelaunchActivated }
        fun sendMetaCapiEvents(sendMetaCapiEvents: Boolean?) = apply { this.sendMetaCapiEvents = sendMetaCapiEvents }
        fun sendThirdPartyEvents(sendThirdPartyEvents: Boolean?) = apply { this.sendThirdPartyEvents = sendThirdPartyEvents }
        fun tags(tags: List<String>?) = apply { this.tags = tags ?: emptyList() }
        fun rewards(rewards: List<Reward>?) = apply { this.rewards = rewards ?: emptyList() }
        fun slug(slug: String?) = apply { this.slug = slug }
        fun staffPick(staffPick: Boolean?) = apply { this.staffPick = staffPick }
        fun staticUsdRate(staticUsdRate: Float?) = apply { this.staticUsdRate = staticUsdRate ?: 0f }
        fun usdExchangeRate(usdExchangeRate: Float?) = apply { this.usdExchangeRate = usdExchangeRate ?: 0f }
        fun state(state: String?) = apply { this.state = state ?: "" }
        fun stateChangedAt(stateChangedAt: DateTime?) = apply { this.stateChangedAt = stateChangedAt }
        fun unreadMessagesCount(unreadMessagesCount: Int?) = apply { this.unreadMessagesCount = unreadMessagesCount ?: 0 }
        fun unseenActivityCount(unseenActivityCount: Int?) = apply { this.unseenActivityCount = unseenActivityCount ?: 0 }
        fun updatedAt(updatedAt: DateTime?) = apply { this.updatedAt = updatedAt }
        fun updatesCount(updatesCount: Int?) = apply { this.updatesCount = updatesCount ?: 0 }
        fun urls(urls: Urls?) = apply { urls?.let { this.urls = it } }
        fun video(video: Video?) = apply { this.video = video }
        fun projectFaqs(projectFaqs: List<ProjectFaq>?) = apply { this.projectFaqs = projectFaqs ?: emptyList() }
        fun envCommitments(envCommitments: List<EnvironmentalCommitment>?) = apply { this.envCommitments = envCommitments ?: emptyList() }
        fun aiDisclosure(aiDisclosure: AiDisclosure?) = apply { this.aiDisclosure = aiDisclosure }
        fun risks(risks: String?) = apply { this.risks = risks ?: "" }
        fun story(story: String?) = apply { this.story = story ?: "" }
        fun isFlagged(isFlagged: Boolean?) = apply { this.isFlagged = isFlagged }
        fun isInPostCampaignPledgingPhase(isInPostCampaignPledgingPhase: Boolean?) = apply { this.isInPostCampaignPledgingPhase = isInPostCampaignPledgingPhase }
        fun postCampaignPledgingEnabled(postCampaignPledgingEnabled: Boolean?) = apply { this.postCampaignPledgingEnabled = postCampaignPledgingEnabled }
        fun build() = Project(
            availableCardTypes = availableCardTypes,
            backersCount = backersCount,
            blurb = blurb,
            backing = backing,
            category = category,
            commentsCount = commentsCount,
            country = country,
            createdAt = createdAt,
            creator = creator,
            currency = currency,
            currencySymbol = currencySymbol,
            currentCurrency = currentCurrency,
            currencyTrailingCode = currencyTrailingCode,
            displayPrelaunch = displayPrelaunch,
            featuredAt = featuredAt,
            friends = friends,
            fxRate = fxRate,
            deadline = deadline,
            goal = goal,
            id = id,
            isBacking = isBacking,
            isPledgeOverTimeAllowed = isPledgeOverTimeAllowed,
            isStarred = isStarred,
            lastUpdatePublishedAt = lastUpdatePublishedAt,
            launchedAt = launchedAt,
            location = location,
            name = name,
            permissions = permissions,
            pledged = pledged,
            photo = photo,
            prelaunchActivated = prelaunchActivated,
            sendMetaCapiEvents = sendMetaCapiEvents,
            sendThirdPartyEvents = sendThirdPartyEvents,
            tags = tags,
            rewards = rewards,
            slug = slug,
            staffPick = staffPick,
            canComment = canComment,
            state = state,
            stateChangedAt = stateChangedAt,
            staticUsdRate = staticUsdRate,
            usdExchangeRate = usdExchangeRate,
            unreadMessagesCount = unreadMessagesCount,
            unseenActivityCount = unseenActivityCount,
            updatesCount = updatesCount,
            updatedAt = updatedAt,
            urls = urls,
            video = video,
            projectFaqs = projectFaqs,
            envCommitments = envCommitments,
            aiDisclosure = aiDisclosure,
            risks = risks,
            story = story,
            isFlagged = isFlagged,
            watchesCount = watchesCount,
            isInPostCampaignPledgingPhase = isInPostCampaignPledgingPhase,
            postCampaignPledgingEnabled = postCampaignPledgingEnabled
        )
    }

    fun toBuilder() = Builder(
        availableCardTypes = availableCardTypes,
        backersCount = backersCount,
        blurb = blurb,
        backing = backing,
        category = category,
        commentsCount = commentsCount,
        country = country,
        createdAt = createdAt,
        creator = creator,
        currency = currency,
        currencySymbol = currencySymbol,
        currentCurrency = currentCurrency,
        currencyTrailingCode = currencyTrailingCode,
        displayPrelaunch = displayPrelaunch,
        featuredAt = featuredAt,
        friends = friends,
        fxRate = fxRate,
        deadline = deadline,
        goal = goal,
        id = id,
        isBacking = isBacking,
        isPledgeOverTimeAllowed = isPledgeOverTimeAllowed,
        isStarred = isStarred,
        lastUpdatePublishedAt = lastUpdatePublishedAt,
        launchedAt = launchedAt,
        location = location,
        name = name,
        permissions = permissions,
        pledged = pledged,
        photo = photo,
        prelaunchActivated = prelaunchActivated,
        sendMetaCapiEvents = sendMetaCapiEvents,
        sendThirdPartyEvents = sendThirdPartyEvents,
        tags = tags,
        rewards = rewards,
        slug = slug,
        staffPick = staffPick,
        canComment = canComment,
        state = state,
        stateChangedAt = stateChangedAt,
        staticUsdRate = staticUsdRate,
        usdExchangeRate = usdExchangeRate,
        unreadMessagesCount = unreadMessagesCount,
        unseenActivityCount = unseenActivityCount,
        updatesCount = updatesCount,
        updatedAt = updatedAt,
        urls = urls,
        video = video,
        projectFaqs = projectFaqs,
        envCommitments = envCommitments,
        aiDisclosure = aiDisclosure,
        risks = risks,
        story = story,
        isFlagged = isFlagged,
        watchesCount = watchesCount,
        isInPostCampaignPledgingPhase = isInPostCampaignPledgingPhase,
        postCampaignPledgingEnabled = postCampaignPledgingEnabled
    )

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        STATE_STARTED,
        STATE_SUBMITTED,
        STATE_LIVE,
        STATE_SUCCESSFUL,
        STATE_FAILED,
        STATE_CANCELED,
        STATE_SUSPENDED,
        STATE_PURGED
    )
    annotation class State

    fun creatorBioUrl() = urls().web().creatorBio()

    fun descriptionUrl() = urls().web().description()

    fun updatesUrl() = urls().web().updates() ?: ""

    fun webProjectUrl() = urls().web().project()

    fun hasComments() = commentsCount().isNonZero()

    fun hasRewards() = rewards()?.isNotEmpty() ?: false

    fun hasVideo() = video() != null

    /** Returns whether the project is in a canceled state.  */
    val isCanceled: Boolean
        get() = STATE_CANCELED == state()

    /** Returns whether the project is in a failed state.  */
    val isFailed: Boolean
        get() = STATE_FAILED == state()

    val isFeaturedToday: Boolean
        get() = featuredAt()?.let {
            DateTimeUtils.isDateToday(it)
        } ?: false

    /** Returns whether the project is in a live state.  */
    val isLive: Boolean
        get() = STATE_LIVE == state()

    val isFriendBacking: Boolean
        get() = friends().isNotEmpty()

    val isFunded: Boolean
        get() = isLive && percentageFunded() >= 100

    /** Returns whether the project is in a purged state.  */
    val isPurged: Boolean
        get() = STATE_PURGED == state()

    /** Returns whether the project is in a live state.  */
    val isStarted: Boolean
        get() = STATE_STARTED == state()

    /** Returns whether the project is in a submitted state.  */
    val isSubmitted: Boolean
        get() = STATE_SUBMITTED == state()

    /** Returns whether the project is in a suspended state.  */
    val isSuspended: Boolean
        get() = STATE_SUSPENDED == state()

    /** Returns whether the project is in a successful state.  */
    val isSuccessful: Boolean
        get() = STATE_SUCCESSFUL == state()

    val isApproachingDeadline: Boolean
        get() = deadline()?.let {
            if (it.isBeforeNow) false
            else it.isBefore(DateTime().plusDays(2))
        } ?: false

    fun percentageFunded(): Float {
        return if (goal() > 0.0f) {
            pledged().toFloat() / goal().toFloat() * 100.0f
        } else 0.0f
    }

    fun param(): String {
        val slug = slug()
        return slug ?: id().toString()
    }

    fun secureWebProjectUrl(): String {
        // TODO: Just use http with local env
        return Uri.parse(webProjectUrl()).buildUpon().scheme("https").build().toString()
    }

    fun newPledgeUrl(): String {
        return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/new")
            .toString()
    }

    fun editPledgeUrl(): String {
        return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/edit")
            .toString()
    }

    override fun toString(): String {
        return (
            "Project{" +
                "id=" + id() + ", " +
                "name=" + name() + ", " +
                "}"
            )
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Project) {
            equals = backersCount() == other.backersCount() &&
                availableCardTypes() == other.availableCardTypes() &&
                blurb() == other.blurb() &&
                backing() == other.backing() &&
                category() == other.category() &&
                commentsCount() == other.commentsCount() &&
                country() == other.country() &&
                createdAt() == other.createdAt() &&
                creator() == other.creator() &&
                createdAt() == other.createdAt() &&
                currency() == other.currency() &&
                currencySymbol() == other.currencySymbol() &&
                currentCurrency() == other.currentCurrency() &&
                currencyTrailingCode() == other.currencyTrailingCode() &&
                displayPrelaunch() == other.displayPrelaunch() &&
                featuredAt() == other.featuredAt() &&
                friends() == other.friends() &&
                deadline() == other.deadline() &&
                fxRate() == other.fxRate() &&
                goal() == other.goal() &&
                id() == other.id() &&
                isBacking() == other.isBacking() &&
                isPledgeOverTimeAllowed() == other.isPledgeOverTimeAllowed() &&
                isStarred() == other.isStarred() &&
                lastUpdatePublishedAt() == other.lastUpdatePublishedAt() &&
                launchedAt() == other.launchedAt() &&
                location() == other.location() &&
                name() == other.name() &&
                permissions() == other.permissions() &&
                pledged() == other.pledged() &&
                photo() == other.photo() &&
                prelaunchActivated() == other.prelaunchActivated() &&
                sendMetaCapiEvents() == other.sendMetaCapiEvents() &&
                sendThirdPartyEvents() == other.sendThirdPartyEvents() &&
                rewards() == other.rewards() &&
                slug() == other.slug() &&
                staffPick() == other.staffPick() &&
                slug() == other.slug() &&
                canComment() == other.canComment() &&
                state() == other.state() &&
                stateChangedAt() == other.stateChangedAt() &&
                staticUsdRate() == other.staticUsdRate() &&
                usdExchangeRate() == other.usdExchangeRate() &&
                unreadMessagesCount() == other.unreadMessagesCount() &&
                unseenActivityCount() == other.unseenActivityCount() &&
                updatesCount() == other.updatesCount() &&
                updatedAt() == other.updatedAt() &&
                urls() == other.urls() &&
                video() == other.video() &&
                projectFaqs() == other.projectFaqs() &&
                envCommitments() == other.envCommitments() &&
                aiDisclosure() == other.aiDisclosure() &&
                risks() == other.risks() &&
                story() == other.story() &&
                isFlagged() == other.isFlagged()
        }
        return equals
    }

    override fun hashCode(): Int {
        return id().toInt()
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()

        const val STATE_STARTED = "started"
        const val STATE_SUBMITTED = "submitted"
        const val STATE_LIVE = "live"
        const val STATE_SUCCESSFUL = "successful"
        const val STATE_FAILED = "failed"
        const val STATE_CANCELED = "canceled"
        const val STATE_SUSPENDED = "suspended"
        const val STATE_PURGED = "purged"
    }
}
