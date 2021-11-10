@file:JvmName("ProjectExt")
package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Config
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.I18nUtils
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import org.joda.time.DateTime
import org.joda.time.Duration
import type.CreditCardTypes
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

/**
 * Checks if the given card type is listed in the available card types
 *
 * @param cardType the given card type
 *
 * @return boolean that represents if the card type is available
 */
fun Project.acceptedCardType(cardType: CreditCardTypes) = this.availableCardTypes()?.contains(cardType.rawValue()) ?: false

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
        this.deadlineCountdownUnit(context)
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
 * Returns `true` if the project is no longer live, `false` otherwise.
 */
fun Project.isCompleted(): Boolean =
    when (this.state()) {
        Project.STATE_CANCELED,
        Project.STATE_FAILED,
        Project.STATE_SUCCESSFUL,
        Project.STATE_PURGED,
        Project.STATE_SUSPENDED -> true
        else -> false
    }

/**
 * Returns `true` if the project name ends with a punctuation character.
 */
fun isProjectNamePunctuated(name: String): Boolean = name.substring(name.length - 1).matches(".*\\p{Punct}".toRegex())

/**
 * Returns the metadata for the project based on some backing, starred, or featured, otherwise returns null
 */
fun Project.metadataForProject(): ProjectMetadata? =
    when {
        this.isBacking -> ProjectMetadata.BACKING
        this.isStarred -> ProjectMetadata.SAVING
        this.isFeaturedToday -> ProjectMetadata.CATEGORY_FEATURED
        else -> null
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
fun Project.canUpdateFulfillment() = isBacking && isSuccessful

enum class ProjectMetadata {
    BACKING, SAVING, CATEGORY_FEATURED
}
