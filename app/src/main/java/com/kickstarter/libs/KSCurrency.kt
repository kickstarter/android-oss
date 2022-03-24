package com.kickstarter.libs

import com.kickstarter.libs.models.Country.Companion.findByCurrencyCode
import com.kickstarter.libs.utils.extensions.trimAllWhitespace
import kotlin.jvm.JvmOverloads
import auto.parcel.AutoParcel
import android.os.Parcelable
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.models.Project
import java.math.RoundingMode

class KSCurrency(private val currentConfig: CurrentConfigType) {
    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue Value to display, local to the project's currency.
     * @param project      The project to use to look up currency information.
     */
    fun format(initialValue: Double, project: Project, roundingMode: RoundingMode): String {
        return format(initialValue, project, true, roundingMode, false)
    }
    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue        Value to display, local to the project's currency.
     * @param project             The project to use to look up currency information.
     * @param excludeCurrencyCode If true, hide the US currency code for US users only.
     */
    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue        Value to display, local to the project's currency.
     * @param project             The project to use to look up currency information.
     */
    /**
     * Returns a currency string appropriate to the user's locale and location relative to a project.
     *
     * @param initialValue        Value to display, local to the project's currency.
     * @param project             The project to use to look up currency information.
     * @param excludeCurrencyCode If true, hide the US currency code for US users only.
     */
    @JvmOverloads
    fun format(
        initialValue: Double,
        project: Project,
        excludeCurrencyCode: Boolean = true,
        roundingMode: RoundingMode = RoundingMode.DOWN,
        currentCurrency: Boolean = false
    ): String {
        val country = findByCurrencyCode(
            (if (currentCurrency) project.currentCurrency() else project.currency())!!
        ) ?: return ""
        val roundedValue = getRoundedValue(initialValue, roundingMode)
        val currencyOptions = currencyOptions(roundedValue, country, excludeCurrencyCode)
        val numberOptions = NumberOptions.builder()
            .currencyCode(currencyOptions.currencyCode())
            .currencySymbol(currencyOptions.currencySymbol())
            .roundingMode(roundingMode)
            .precision(NumberUtils.precision(initialValue, roundingMode))
            .build()
        return NumberUtils.format(currencyOptions.value(), numberOptions).trimAllWhitespace()
    }

    /**
     * Returns a currency string appropriate to the user's locale and preferred currency.
     *
     * @param initialValue Value to convert, local to the project's currency.
     * @param project The project to use to look up currency information.
     */
    fun formatWithUserPreference(initialValue: Double, project: Project): String {
        return formatWithUserPreference(initialValue, project, RoundingMode.DOWN, 0)
    }

    /**
     * Returns a currency string appropriate to the user's locale and preferred currency.
     *
     * @param initialValue Value to convert, local to the project's currency.
     * @param project The project to use to look up currency information.
     * @param roundingMode This determines whether we should round the values down or up.
     * @param precision How much of the change we should show.
     */
    fun formatWithUserPreference(
        initialValue: Double, project: Project,
        roundingMode: RoundingMode, precision: Int
    ): String {
        val country = findByCurrencyCode(
            project.currentCurrency()!!
        ) ?: return ""
        val convertedValue = getRoundedValue(initialValue, roundingMode) * project.fxRate()
        val currencyOptions = currencyOptions(convertedValue, country, true)
        val numberOptions = NumberOptions.builder()
            .currencySymbol(currencyOptions.currencySymbol())
            .roundingMode(roundingMode)
            .precision(if (precision > 0) 2 else 0)
            .build()
        return NumberUtils.format(currencyOptions.value(), numberOptions).trimAllWhitespace()
    }

    /**
     * Returns a boolean determining if a country's currency is ambiguous.
     * Special case: US people looking at US currency just get the currency symbol.
     *
     * @param country The country to check if a code is necessary.
     * @param excludeCurrencyCode If true, hide the US currency code for US users only.
     */
    fun currencyNeedsCode(country: Country, excludeCurrencyCode: Boolean): Boolean {
        val countryIsUS = country === Country.US
        val config = currentConfig.observable()
            .toBlocking()
            .first()
        val currencyNeedsCode = config.currencyNeedsCode(country.currencySymbol)
        val userInUS = config.countryCode() == Country.US.countryCode
        return if (userInUS && excludeCurrencyCode && countryIsUS) {
            false
        } else {
            currencyNeedsCode
        }
    }

    /**
     * Build [CurrencyOptions] based on the country.
     */
    private fun currencyOptions(
        value: Float,
        country: Country,
        excludeCurrencyCode: Boolean
    ): CurrencyOptions {
        return CurrencyOptions.builder()
            .country(country.countryCode)
            .currencyCode("")
            .currencySymbol(getCurrencySymbol(country, excludeCurrencyCode))
            .value(value)
            .build()
    }

    /**
     * Returns the currency symbol for a country.
     *
     * @param country The country the currency will be displayed in.
     * @param excludeCurrencyCode If true, hide the US currency code for US users only.
     */
    fun getCurrencySymbol(country: Country, excludeCurrencyCode: Boolean): String {
        return if (!currencyNeedsCode(country, excludeCurrencyCode)) {
            country.currencySymbol
        } else if (country === Country.SG) {
            // Singapore projects get a special currency prefix
            "\u00A0" + "S" + country.currencySymbol + "\u00A0"
        } else if (country.currencySymbol == "kr" || country.currencySymbol == "Fr") {
            // Kroner projects use the currency code prefix
            "\u00A0" + country.currencyCode + "\u00A0"
        } else {
            "\u00A0" + country.countryCode + country.currencySymbol + "\u00A0"
        }
    }

    @AutoParcel
    abstract class CurrencyOptions : Parcelable {
        abstract fun country(): String?
        abstract fun currencyCode(): String?
        abstract fun currencySymbol(): String?
        abstract fun value(): Float

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun country(__: String?): Builder
            abstract fun currencyCode(__: String?): Builder
            abstract fun currencySymbol(__: String?): Builder
            abstract fun value(__: Float): Builder
            abstract fun build(): CurrencyOptions
        }

        abstract fun toBuilder(): Builder?

        companion object {
            fun builder(): Builder {
                return AutoParcel_KSCurrency_CurrencyOptions.Builder()
            }
        }
    }

    companion object {
        /**
         * Returns a number rounded to the specification.
         *
         * @param initialValue Value to convert, local to the project's currency.
         * @param roundingMode When this is DOWN, we get the floor of the initialValue.
         */
        private fun getRoundedValue(initialValue: Double, roundingMode: RoundingMode): Float {
            return if (roundingMode == RoundingMode.DOWN) {
                Math.floor(initialValue).toFloat()
            } else {
                initialValue.toFloat()
            }
        }
    }
}