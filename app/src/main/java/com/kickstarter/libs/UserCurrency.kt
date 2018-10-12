package com.kickstarter.libs

import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.models.Project
import type.CurrencyCode
import java.math.RoundingMode

class UserCurrency(private val currentConfigType: CurrentConfigType) {

    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project The project to use to look up currency information.
     */
    fun format(initialValue: Float, project: Project, symbol: String): String {
        return format(initialValue, project, false, RoundingMode.DOWN, symbol)
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
               excludeCurrencyCode: Boolean, symbol: String): String {

        return format(initialValue, project, excludeCurrencyCode, RoundingMode.DOWN, symbol)
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
               excludeCurrencyCode: Boolean, roundingMode: RoundingMode, symbol: String): String {

        val currencyOptions = userCurrencyOptions(initialValue, project, symbol)

        val showCurrencyCode = showCurrencyCode(currencyOptions, excludeCurrencyCode)

        val numberOptions = NumberOptions.builder()
                .currencyCode(if (showCurrencyCode) currencyOptions.currencyCode() else "")
                .currencySymbol(currencyOptions.currencySymbol())
                .roundingMode(roundingMode)
                .build()

        return NumberUtils.format(currencyOptions.value(), numberOptions)
    }

    private fun userCurrencyOptions(value: Float, project: Project, symbol: String): KSCurrency.CurrencyOptions {
        val fxRate = project.fx_rate()
        val config = this.currentConfigType.getConfig()

        return if (config.countryCode() == "XX") {
            KSCurrency.CurrencyOptions.builder()
                    .country(project.country())
                    .currencyCode("")
                    .currencySymbol("US$ ")
                    .value(value * fxRate!!)
                    .build()
        } else {
            KSCurrency.CurrencyOptions.builder()
                    .country(project.country())
                    .currencyCode("")
                    .currencySymbol(currencySymbol(symbol))
                    .value(value * fxRate!!)
                    .build()
        }

    }

    private fun currencySymbol(chosenCurrency: String): String {
        val symbol: String

        when (chosenCurrency) {
            CurrencyCode.AUD.rawValue() -> symbol = "AU$ "
            CurrencyCode.CAD.rawValue() -> symbol = "CA$ "
            CurrencyCode.CHF.rawValue() -> symbol = "CHF "
            CurrencyCode.DKK.rawValue() -> symbol = "DKK "
            CurrencyCode.EUR.rawValue() -> symbol = "€ "
            CurrencyCode.GBP.rawValue() -> symbol = "£ "
            CurrencyCode.HKD.rawValue() -> symbol = "HK$ "
            CurrencyCode.JPY.rawValue() -> symbol = "¥ "
            CurrencyCode.MXN.rawValue() -> symbol = "MX$ "
            CurrencyCode.NOK.rawValue() -> symbol = "NOK "
            CurrencyCode.NZD.rawValue() -> symbol = "NZ$ "
            CurrencyCode.SEK.rawValue() -> symbol = "SEK "
            CurrencyCode.SGD.rawValue() -> symbol = "S$ "
            CurrencyCode.USD.rawValue() -> symbol = "$ "

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
