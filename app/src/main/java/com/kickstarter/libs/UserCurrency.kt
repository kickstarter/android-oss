package com.kickstarter.libs

import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.models.Project
import com.kickstarter.models.User
import java.math.RoundingMode

class UserCurrency(private val user: User, private val currentConfigType: CurrentConfigType) {

    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project The project to use to look up currency information.
     */
    fun format(initialValue: Float, project: Project): String {
        return format(initialValue, project, false, RoundingMode.DOWN)
    }

    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project The project to use to look up currency information.
     * @param excludeCurrencyCode If true, hide the currency code, even if that makes the returned value ambiguous.
     * This is used when space is constrained and the currency code can be determined elsewhere.
     */
    fun format(initialValue: Float, project: Project,
               excludeCurrencyCode: Boolean): String {

        return format(initialValue, project, excludeCurrencyCode, RoundingMode.DOWN)
    }

    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project The project to use to look up currency information.
     * @param excludeCurrencyCode If true, hide the currency code, even if that makes the returned value ambiguous.
     * This is used when space is constrained and the currency code can be determined elsewhere.
     * @param preferUSD Attempt to convert a project from it's local currency to USD, if the user is located in
     * the US.
     */
    fun format(initialValue: Float, project: Project,
               excludeCurrencyCode: Boolean, roundingMode: RoundingMode): String {

        val currencyOptions = userCurrencyOptions(initialValue, project)

        val showCurrencyCode = showCurrencyCode(currencyOptions, excludeCurrencyCode)

        val numberOptions = NumberOptions.builder()
                .currencyCode(if (showCurrencyCode) currencyOptions.currencyCode() else "")
                .currencySymbol(currencyOptions.currencySymbol())
                .roundingMode(roundingMode)
                .build()

        return NumberUtils.format(currencyOptions.value(), numberOptions)
    }

    private fun userCurrencyOptions(value: Float, project: Project): KSCurrency.CurrencyOptions {

        val fxRate = project.fx_rate()

        return KSCurrency.CurrencyOptions.builder()
                .country(project.country())
                .currencyCode("")
                .currencySymbol(currencySymbol(user))
                .value(value * fxRate!!)
                .build()

    }

    private fun currencySymbol(user: User): String {
        val symbol: String

        when (user.chosenCurrency()) {
            "AUD" -> symbol = "AU$ "
            "CAD" -> symbol = "CA$ "
            "CHF" -> symbol = "CHF "
            "DKK" -> symbol = "DKK "
            "EUR" -> symbol = "€ "
            "GBP" -> symbol = "£ "
            "HKD" -> symbol = "HK$ "
            "JPY" -> symbol = "¥ "
            "MXN" -> symbol = "MX$ "
            "NOK" -> symbol = "NOK "
            "NZD" -> symbol = "NZ$ "
            "SEK" -> symbol = "SEK "
            "SGD" -> symbol = "S$ "
            "USD" -> symbol = "$ "

            else -> symbol = "$ "
        }
        return symbol
    }

    /**
     * Determines whether the currency code should be shown. If the currency is ambiguous (e.g. CAD and USD both use `$`),
     * we show the currency code if the user is not in the US, or the project is not in the US.
     */
    private fun showCurrencyCode(currencyOptions: KSCurrency.CurrencyOptions, excludeCurrencyCode: Boolean): Boolean {
        if (excludeCurrencyCode) {
            return false
        }

        val config = this.currentConfigType.config
        val currencyIsDupe = config.currencyNeedsCode(currencyOptions.currencySymbol())
        val userIsUS = config.countryCode() == "US"
        val projectIsUS = currencyOptions.country() == "US"

        return currencyIsDupe && !userIsUS || currencyIsDupe && !projectIsUS
    }
}