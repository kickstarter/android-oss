@file:JvmName("ProjectExt")

package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Config
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.I18nUtils
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.User
import com.kickstarter.models.Web
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.CreditCardTypes
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Duration
import kotlin.math.floor

/**
 * When fetching a project from GraphQL, we need to populate the next fields
 *     Project.currentCurrency()
 *     Project.currencyTrailingCode()
 *     Project.currencySymbol()
 *
 * - with the country code on configuration in case no user logged in
 * - with the selected currency by the user in case the is logged in user.
 *
 * Note: And user logged in can change it's currency at any time.
 */
fun Project.updateProjectWith(config: Config, user: User?): Project {
    val currentCountry = config.launchedCountries().find {
        it.name().equals(config.countryCode())
    }

    val currentCurrency = user?.let {
        it.chosenCurrency()
    } ?: currentCountry?.currencyCode() ?: currency()

    val countryOfCurrency = config.launchedCountries().first { it.currencyCode() == currentCurrency }
    val currencySymbol = countryOfCurrency.currencySymbol()
    val trailingCode = countryOfCurrency.trailingCode()

    return this.toBuilder()
        .currentCurrency(currentCurrency)
        .currencyTrailingCode(trailingCode ?: false)
        .currencySymbol(currencySymbol)
        .build()
}

fun Project.showLatePledgeFlow() = this.isInPostCampaignPledgingPhase() ?: false && this.postCampaignPledgingEnabled() ?: false && !this.isBacking()

/**
 * Checks if the given card type is listed in the available card types
 *
 * @param cardType the given card type
 *
 * @return boolean that represents if the card type is available
 */
fun Project.acceptedCardType(cardType: CreditCardTypes?) = this.availableCardTypes()?.contains(cardType?.rawValue) ?: false

/**
 * Combines each project in the list with the discovery param
 *
 * @param projects the list of projects
 * @param params the discovery params
 *
 * @return an arraylist of pairs of projects and discovery params
 */
fun combineProjectsAndParams(projects: List<Project>, params: DiscoveryParams): ArrayList<Pair<Project, DiscoveryParams>> {
    val projectAndParams = arrayListOf<Pair<Project, DiscoveryParams>>()
    projects.forEach {
        projectAndParams.add(Pair.create(it, params))
    }
    return projectAndParams
}

/**
 * Returns time until project reaches deadline along with the unit,
 * e.g. `25 minutes`, `8 days`.
 */
fun Project.deadlineCountdown(context: Context): String {
    return StringBuilder().append(this.deadlineCountdownValue())
        .append(" ")
        .append(this.deadlineCountdownUnit(context))
        .toString()
}

/**
 * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
 *
 * @param context an Android context.
 * @param ksString the KSString.
 *
 * @return the resulting String.
 */
fun Project.deadlineCountdownDetail(context: Context, ksString: KSString): String =
    ksString.format(
        context.getString(R.string.discovery_baseball_card_time_left_to_go),
        "time_left",
        this.deadlineCountdownUnit(context),
    )

/**
 * Returns the most appropriate unit for the time remaining until the project
 * reaches its deadline.
 *
 * @param context an Android context.
 * @return the String unit.
 */
fun Project.deadlineCountdownUnit(context: Context): String {
    val seconds = this.timeInSecondsUntilDeadline()

    return when {
        seconds in 0..1 -> context.getString(R.string.discovery_baseball_card_deadline_units_secs)
        seconds <= 120.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_secs)
        seconds <= 120.0 * 60.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_mins)
        seconds <= 72.0 * 60.0 * 60.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_hours)
        else -> context.getString(R.string.discovery_baseball_card_deadline_units_days)
    }
}

/**
 * Returns time remaining until project reaches deadline in either seconds,
 * minutes, hours or days. A time unit is chosen such that the number is
 * readable, e.g. 5 minutes would be preferred to 300 seconds.
 *
 * @return the Integer time remaining.
 */
fun Project.deadlineCountdownValue(): Int {
    val seconds = this.timeInSecondsUntilDeadline()

    return when {
        seconds <= 120.0 -> seconds.toInt() // seconds
        seconds <= 120.0 * 60.0 -> floor(seconds / 60.0).toInt() // minutes
        seconds < 72.0 * 60.0 * 60.0 -> floor(seconds / 60.0 / 60.0).toInt() // hours
        else -> floor(seconds / 60.0 / 60.0 / 24.0).toInt() // days
    }
}

/**
 * The project is allowed to pledges during crowdfund active campaign or late pledges phase
 */
fun Project.isAllowedToPledge(): Boolean {
    return (!this.isCompleted() || this.showLatePledgeFlow())
}

/**
 * Returns `true` if the project is no longer live, `false` otherwise.
 */
fun Project.isCompleted(): Boolean =
    when (this.state()) {
        Project.STATE_CANCELED,
        Project.STATE_FAILED,
        Project.STATE_SUCCESSFUL,
        Project.STATE_PURGED,
        Project.STATE_SUSPENDED,
        -> true
        else -> false
    }

/**
 * Returns `true` if the project name ends with a punctuation character.
 */
fun isProjectNamePunctuated(name: String): Boolean = name.substring(name.length - 1).matches(".*\\p{Punct}".toRegex())

/**
 * Returns the metadata for the project based on some backing, starred, or featured, otherwise returns null
 */
fun Project.metadataForProject(): ProjectMetadata =
    when {
        this.isBacking() -> ProjectMetadata.BACKING
        this.displayPrelaunch() ?: false -> ProjectMetadata.COMING_SOON
        this.isStarred() -> ProjectMetadata.SAVING
        this.isFeaturedToday -> ProjectMetadata.CATEGORY_FEATURED
        else -> ProjectMetadata.NONE
    }

/**
 * Returns 16:9 height relative to input width.
 */
fun photoHeightFromWidthRatio(width: Int): Int = width * 9 / 16

/**
 * Returns time between project launch and deadline.
 */
fun Project.timeInSecondsOfDuration(): Long = Duration(this.launchedAt(), this.deadline()).standardSeconds

/**
 * Returns time between project launch and deadline.
 */
fun Project.timeInDaysOfDuration(): Long = Duration(this.launchedAt(), this.deadline()).standardDays

/**
 * Returns time until project reaches deadline in seconds, or 0 if the
 * project has already finished.
 */
fun Project.timeInSecondsUntilDeadline(): Long =
    0L.coerceAtLeast(Duration(DateTime(), this.deadline()).standardSeconds)

/**
 * Returns if the current user is the creator of the project
 */
fun Project.userIsCreator(user: User?): Boolean = user?.let { this.creator().id() == it.id() } ?: false

fun isUSUserViewingNonUSProject(userCountry: String, projectCountry: String): Boolean = I18nUtils.isCountryUS(userCountry) && !I18nUtils.isCountryUS(projectCountry)

/**
 * In order to update the fulfillment state for a project
 * the the user needs to be a backer, and the project successful
 */
fun Project.canUpdateFulfillment() = isBacking() && isSuccessful

enum class ProjectMetadata {
    BACKING, SAVING, CATEGORY_FEATURED, COMING_SOON, NONE
}

/**
 * Given a list of projects and root categories this will determine if the first project is featured
 * and is in need of its root category. If that is the case we will find its root and fill in that
 * data and return a new list of projects.
 */
fun List<Project>.fillRootCategoryForFeaturedProjects(rootCategories: List<Category>): List<Project> {
    // Guard against no projects
    if (this.isEmpty()) {
        return ListUtils.empty()
    }
    val firstProject = this[0]

    // Guard against bad category data on first project
    val category = firstProject.category() ?: return this
    val categoryParentId = category.parentId() ?: return this

    // Guard against not needing to find the root category
    if (!firstProject.projectNeedsRootCategory(category)) {
        return this
    }

    // Find the root category for the featured project's category
    val projectRootCategory = Observable.fromIterable(rootCategories)
        .filter { rootCategory: Category -> rootCategory.id() == categoryParentId }
        .take(1)
        .blockingFirst()

    // Sub in the found root category in our featured project.
    val newCategory = category.toBuilder().parent(projectRootCategory).build()
    val newProject = firstProject.toBuilder().category(newCategory).build()
    return ListUtils.replaced(this, 0, newProject)
}

/**
 * Determines if the project and supplied require us to find the root category.
 */
fun Project.projectNeedsRootCategory(category: Category): Boolean {
    return !category.isRoot && category.parent() == null && this.isFeaturedToday
}

fun Project.updateStartedProjectAndDiscoveryParamsList(
    listOfProjects: List<Pair<Project, DiscoveryParams>>,
): List<Pair<Project, DiscoveryParams>> {
    val position = listOfProjects.indexOfFirst { item ->
        item.first.id() == this.id()
    }

    if (position >= 0 && position < listOfProjects.size) {
        return listOfProjects.toMutableList().apply {
            val project = listOfProjects[position].first.toBuilder().isStarred(this@updateStartedProjectAndDiscoveryParamsList.isStarred()).build()
            this[position] = Pair(project, listOfProjects[position].second)
        }
    }

    return listOfProjects
}

/**
 * Extension function that will return a reduced copy of the the target project, the fields available
 * on the reduce copy are those ones required on the next Screens: BackingAddons, PledgeFragment, ThanksActivity
 *
 * The end goal is to reduce to the bare minimum the amount of memory required to be serialized on Intents
 * when presenting screens in order to avoid `android.os.TransactionTooLargeException`
 */
fun Project.reduce(): Project {
    return Project.Builder()
        .id(this.id())
        .slug(this.slug())
        .name(this.name())
        .location(this.location())
        .deadline(this.deadline())
        .staticUsdRate(this.staticUsdRate())
        .fxRate(this.fxRate())
        .country(this.country())
        .currentCurrency(this.currentCurrency())
        .currency(this.currency())
        .currencySymbol(this.currencySymbol())
        .currencyTrailingCode(this.currencyTrailingCode())
        .sendThirdPartyEvents(this.sendThirdPartyEvents())
        .isBacking(this.isBacking())
        .backing(backing())
        .availableCardTypes(this.availableCardTypes())
        .category(this.category())
        .build()
}

/**
 * Extension function that will return a reduced copy of the the target project, the fields available
 * on the reduce copy are those ones required on the next Screens: BackingAddons, PledgeFragment, ThanksActivity
 *
 * The end goal is to reduce to the bare minimum the amount of memory required to be serialized on Intents
 * when presenting screens in order to avoid `android.os.TransactionTooLargeException`
 */
fun Project.reduceProjectPayload(): Project {
    val web = Web.builder()
        .project(this.webProjectUrl())
        .build()

    return Project.Builder()
        .id(this.id())
        .canComment(this.canComment())
        .slug(this.slug())
        .name(this.name())
        .creator(this.creator())
        .blurb(this.blurb())
        .location(this.location())
        .category(this.category())
        .watchesCount(this.watchesCount())
        .photo(this.photo())
        .country(this.country())
        .currentCurrency(this.currentCurrency())
        .sendThirdPartyEvents(this.sendThirdPartyEvents())
        .isStarred(this.isStarred())
        .isBacking(this.isBacking())
        .currency(this.currency())
        .currencySymbol(this.currencySymbol())
        .currencyTrailingCode(this.currencyTrailingCode())
        .urls(Urls.builder().web(web).build())
        .isInPostCampaignPledgingPhase(this.isInPostCampaignPledgingPhase())
        .postCampaignPledgingEnabled(this.postCampaignPledgingEnabled())
        .sendThirdPartyEvents(this.sendThirdPartyEvents())
        .state(this.state())
        .build()
}
