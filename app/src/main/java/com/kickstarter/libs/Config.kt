package com.kickstarter.libs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Config private constructor(
    private val abExperiments: Map<String, String>?,
    private val countryCode: String,
    private val features: Map<String, Boolean>?,
    private val launchedCountries: List<LaunchedCountry>,
) : Parcelable {

    fun abExperiments() = this.abExperiments
    fun countryCode() = this.countryCode
    fun features() = this.features
    fun launchedCountries() = this.launchedCountries

    @Parcelize
    data class Builder(
        private var abExperiments: Map<String, String>? = null,
        private var countryCode: String = "",
        private var features: Map<String, Boolean>? = null,
        private var launchedCountries: List<LaunchedCountry> = emptyList(),
    ) : Parcelable {
        fun abExperiments(abExperiments: Map<String, String>?) = apply { this.abExperiments = abExperiments }
        fun countryCode(countryCode: String) = apply { this.countryCode = countryCode }
        fun features(features: Map<String, Boolean>?) = apply { this.features = features }
        fun launchedCountries(launchedCountries: List<LaunchedCountry> = emptyList()) = apply { this.launchedCountries = launchedCountries }
        fun build() = Config(
            abExperiments = abExperiments,
            countryCode = countryCode,
            features = features,
            launchedCountries = launchedCountries
        )
    }

    fun toBuilder() = Builder(
        abExperiments = abExperiments,
        countryCode = countryCode,
        features = features,
        launchedCountries = launchedCountries
    )

    @Parcelize
    class LaunchedCountry private constructor(
        private val name: String?,
        private val currencyCode: String?,
        private val currencySymbol: String,
        private val trailingCode: Boolean
    ) : Parcelable {

        fun name(): String? = this.name
        fun currencyCode(): String? = this.currencyCode
        fun currencySymbol(): String = this.currencySymbol
        fun trailingCode(): Boolean = this.trailingCode

        @Parcelize
        data class Builder(
            private var name: String? = null,
            private var currencyCode: String? = null,
            private var currencySymbol: String = "",
            private var trailingCode: Boolean = false,
        ) : Parcelable {
            fun name(name: String?) = apply { this.name = name }
            fun currencyCode(currencyCode: String?) = apply { this.currencyCode = currencyCode }
            fun currencySymbol(currencySymbol: String) = apply { this.currencySymbol = currencySymbol }
            fun trailingCode(trailingCode: Boolean) = apply { this.trailingCode = trailingCode }

            fun build() = LaunchedCountry(
                name = name,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                trailingCode = trailingCode
            )
        }

        fun toBuilder() = Builder(
            name = name,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            trailingCode = trailingCode
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is LaunchedCountry) {
                equals = name() == obj.name() &&
                    currencyCode() == obj.currencyCode() &&
                    currencySymbol() == obj.currencySymbol() &&
                    trailingCode() == obj.trailingCode()
            }
            return equals
        }
    }

    /**
     * A currency needs a code if its symbol is ambiguous, e.g. `$` is used for currencies such as USD, CAD, AUD.
     */
    fun currencyNeedsCode(currencySymbol: String): Boolean {
        for (country in launchedCountries()) {
            if (country.currencySymbol() == currencySymbol) {
                return country.trailingCode()
            }
        }

        // Unlaunched country, default to showing the code.
        return true
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Config) {
            equals = abExperiments() == obj.abExperiments() &&
                countryCode() == obj.countryCode() &&
                features() == obj.features() &&
                launchedCountries() == obj.launchedCountries()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder().features(mutableMapOf())
    }
}
