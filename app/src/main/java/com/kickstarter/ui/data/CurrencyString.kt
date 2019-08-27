package com.kickstarter.ui.data

import android.text.SpannableString

data class CurrencyString(val amount: Double, val currencySymbol: SpannableString, val symbolAtStart: Boolean = true)