package com.kickstarter.libs.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Pair
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.models.Country
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import java.math.RoundingMode

object RewardViewUtils {
    /**
     * Returns the string resource ID of the rewards button based on project and reward status.
     */
    @StringRes
    fun pledgeButtonText(project: Project, reward: Reward): Int {
        return when {
            BackingUtils.isBacked(project, reward) -> R.string.Selected
            RewardUtils.isAvailable(project, reward) -> R.string.Select
            else -> R.string.No_longer_available
        }
    }

    /**
     * Returns the shipping summary for a reward.
     */
    fun shippingSummary(context: Context, ksString: KSString, stringResAndLocationName: Pair<Int, String?>): String {
        val stringRes = stringResAndLocationName.first
        val locationName = stringResAndLocationName.second
        val shippingSummary = context.getString(stringRes)

        return when (stringRes) {
            R.string.location_name_only -> when (locationName) {
                null -> context.getString(R.string.Limited_shipping)
                else -> ksString.format(shippingSummary, "location_name", locationName)
            }
            else -> context.getString(stringRes)
        }
    }

    /**
     * Returns a SpannableString representing currency that shrinks currency symbol if it's necessary.
     * Special case: US people looking at US currency just get the currency symbol.
     *
     */
    fun styleCurrency(value: Double, project: Project, ksCurrency: KSCurrency): SpannableString {
        val formattedCurrency = ksCurrency.format(value, project, RoundingMode.HALF_UP)
        val spannableString = SpannableString(formattedCurrency)

        val country = Country.findByCurrencyCode(project.currency()) ?: return spannableString

        val currencyNeedsCode = ksCurrency.currencyNeedsCode(country, true)
        val currencySymbolToDisplay = StringUtils.trim(ksCurrency.getCurrencySymbol(country, true))

        if (currencyNeedsCode) {
            val startOfSymbol = formattedCurrency.indexOf(currencySymbolToDisplay)
            val endOfSymbol = startOfSymbol + currencySymbolToDisplay.length
            spannableString.setSpan(RelativeSizeSpan(.7f), startOfSymbol, endOfSymbol, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        return spannableString
    }
}
