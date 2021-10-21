@file:JvmName("ProjectExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.libs.Config
import com.kickstarter.models.Project
import com.kickstarter.models.User

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
 * In order to update the fulfillment state for a project
 * the the user needs to be a backer, and the project successful
 */
fun Project.canUpdateFulfillment() = isBacking && isSuccessful
