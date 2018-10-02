package com.kickstarter.models

import android.content.res.Resources
import com.kickstarter.R
import type.CurrencyCode

enum class CurrencyEnum(private val currencyCode: CurrencyCode, val stringResId: Int) {
    AUD(CurrencyCode.AUD, R.string.Currency_AUD),
    CAD(CurrencyCode.CAD, R.string.Currency_CAD),
    CHF(CurrencyCode.CHF, R.string.Currency_CHF),
    DKK(CurrencyCode.DKK, R.string.Currency_DKK),
    EUR(CurrencyCode.EUR, R.string.Currency_EUR),
    GBP(CurrencyCode.GBP, R.string.Currency_GBP),
    HKD(CurrencyCode.HKD, R.string.Currency_HKD),
    JPY(CurrencyCode.JPY, R.string.Currency_JPY),
    MXN(CurrencyCode.MXN, R.string.Currency_MXN),
    NOK(CurrencyCode.NOK, R.string.Currency_NOK),
    NZD(CurrencyCode.NZD, R.string.Currency_NZD),
    SEK(CurrencyCode.SEK, R.string.Currency_SEK),
    SGD(CurrencyCode.SGD, R.string.Currency_SGD),
    USD(CurrencyCode.USD, R.string.Currency_USD);


    companion object {
        fun getCurrencies(resources: Resources): Array<String?> {
            val strings = kotlin.arrayOfNulls<String>(values().size)
            val currencies = CurrencyEnum.values()

            var i = 0
            val length = values().size
            while (i < length) {
                val currencyEnum = currencies[i]
                strings[i] = resources.getString(currencyEnum.stringResId)
                i++
            }
            return strings
        }
    }
}
