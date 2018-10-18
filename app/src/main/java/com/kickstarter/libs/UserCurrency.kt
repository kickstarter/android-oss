package com.kickstarter.libs

import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.models.Project
import type.CurrencyCode
import java.math.RoundingMode

class UserCurrency(private val currentConfigType: CurrentConfigType) {

    /**
     * Returns a currency string appropriate to the user's locale and preferred currency.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project The project to use to look up currency information.
     * @param excludeCurrencyCode If true, hide the currency code, even if that makes the returned value ambiguous.
     * This is used when space is constrained and the currency code can be determined elsewhere.
     */
    fun format(initialValue: Float, project: Project, symbol: String): String {

        return format(initialValue, project, RoundingMode.DOWN, symbol)
    }

    /**
     * Returns a currency string appropriate to the user's locale and preferred currency.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project The project to use to look up currency information.
     * @param excludeCurrencyCode If true, hide the currency code, even if that makes the returned value ambiguous.
     * This is used when space is constrained and the currency code can be determined elsewhere.
     */
    fun format(initialValue: Float, project: Project, roundingMode: RoundingMode, symbol: String): String {

        val currencyOptions = userCurrencyOptions(initialValue, project, symbol)

        val numberOptions = NumberOptions.builder()
                .currencySymbol(currencyOptions.currencySymbol())
                .roundingMode(roundingMode)
                .build()

        return NumberUtils.format(currencyOptions.value(), numberOptions)
    }

    /** Show's the project in the user's preferred currency. If the user has no preferred currency the project is shown
     * in $ as a default if the user is in the US. If the user is loacated outside of the US the default will show as
     * $US.
     */
    private fun userCurrencyOptions(value: Float, project: Project, symbol: String): KSCurrency.CurrencyOptions {
        val fxRate = project.fxRate()
        val config = this.currentConfigType.getConfig()

        return if (config.countryCode() == "XX") {
            KSCurrency.CurrencyOptions.builder()
                    .country(project.country())
                    .currencyCode("")
                    .currencySymbol("US$")
                    .value(value * fxRate!!)
                    .build()
        } else {
            KSCurrency.CurrencyOptions.builder()
                    .country(project.country())
                    .currencyCode("")
                    .currencySymbol(currencySymbol(symbol))
                    .value(value * fxRate)
                    .build()
        }

    }

    /** Returns the proper currency symbol based on the user's chosenCurrency preference.  */
    private fun currencySymbol(chosenCurrency: String): String {
        val symbol: String
        val config = this.currentConfigType.getConfig()

        if (config.countryCode() == "US" && chosenCurrency == CurrencyCode.USD.rawValue()) {
            symbol = "$"
            return symbol
        }

        when (chosenCurrency) {
            CurrencyCode.AUD.rawValue() -> symbol = "AU$"
            CurrencyCode.CAD.rawValue() -> symbol = "CA$"
            CurrencyCode.CHF.rawValue() -> symbol = "CHF"
            CurrencyCode.DKK.rawValue() -> symbol = "DKK"
            CurrencyCode.EUR.rawValue() -> symbol = "€"
            CurrencyCode.GBP.rawValue() -> symbol = "£"
            CurrencyCode.HKD.rawValue() -> symbol = "HK$"
            CurrencyCode.JPY.rawValue() -> symbol = "¥"
            CurrencyCode.MXN.rawValue() -> symbol = "MX$"
            CurrencyCode.NOK.rawValue() -> symbol = "NOK"
            CurrencyCode.NZD.rawValue() -> symbol = "NZ$"
            CurrencyCode.SEK.rawValue() -> symbol = "SEK"
            CurrencyCode.SGD.rawValue() -> symbol = "S$"
            CurrencyCode.USD.rawValue() -> symbol = "US$"

            else -> symbol = "US$"
        }
        return symbol
    }
}
