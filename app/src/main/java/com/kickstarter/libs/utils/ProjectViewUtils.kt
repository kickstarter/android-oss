package com.kickstarter.libs.utils

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.util.Pair
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.trimAllWhitespace
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.views.CenterSpan
import java.math.RoundingMode

object ProjectViewUtils {

    /**
     * Returns the color resource ID of the pledge action button based on project and backing status.
     */
    @ColorRes
    fun pledgeActionButtonColor(project: Project, currentUser: User?): Int {
        return if (project.isBacking && project.isLive) {
            R.color.button_pledge_manage
        } else if (!project.isLive || project.creator().id() == currentUser?.id()) {
            when {
                project.backing()?.let { backing -> backing.isErrored() } == true -> R.color.button_pledge_error
                else -> R.color.button_pledge_ended
            }
        } else {
            R.color.button_pledge_live
        }
    }

    fun pledgeActionButtonText(project: Project, currentUser: User?): Int {
        return pledgeActionButtonText(project, currentUser, null)
    }

    fun pledgeActionButtonText(project: Project, currentUser: User?, variant: OptimizelyExperiment.Variant?): Int {
        return if (project.creator().id() == currentUser?.id()) {
            R.string.View_your_rewards
        } else if (!project.isBacking && project.isLive) {
            when (variant) {
                OptimizelyExperiment.Variant.VARIANT_1 -> R.string.See_the_rewards
                OptimizelyExperiment.Variant.VARIANT_2 -> R.string.View_the_rewards
                else -> R.string.Back_this_project
            }
        } else if (project.isBacking && project.isLive) {
            R.string.Manage
        } else if (project.isBacking && !project.isLive) {
            when {
                project.backing()?.let { backing -> backing.isErrored() } == true -> R.string.Manage
                else -> R.string.View_your_pledge
            }
        } else {
            R.string.View_rewards
        }
    }

    @StringRes
    fun pledgeToolbarTitle(project: Project, currentUser: User?): Int {
        return if (project.creator().id() == currentUser?.id()) {
            R.string.View_your_rewards
        } else if (!project.isBacking && project.isLive) {
            R.string.Back_this_project
        } else if (project.isBacking && project.isLive) {
            R.string.Manage_your_pledge
        } else if (project.isBacking && !project.isLive) {
            R.string.View_your_pledge
        } else {
            R.string.View_rewards
        }
    }

    /**
     * Returns a Pair of SpannableString and a Boolean where the
     * SpannableString represents a project's currency symbol that shrinks currency symbol if it's necessary and the
     * Boolean represents whether the symbol should be shown at the end or the start of the currency.
     * Special case: US people looking at US currency just get the currency symbol.
     *
     */
    fun currencySymbolAndPosition(project: Project, ksCurrency: KSCurrency): Pair<SpannableString, Boolean> {
        val formattedCurrency = ksCurrency.format(0.0, project, RoundingMode.HALF_UP)

        val country = Country.findByCurrencyCode(project.currency()) ?: return Pair.create(SpannableString(""), true)

        val currencySymbolToDisplay = ksCurrency.getCurrencySymbol(country, true)

        var symbolAtStart = true
        if (formattedCurrency.endsWith(currencySymbolToDisplay.trimAllWhitespace())) {
            symbolAtStart = false
        }

        val spannableString = SpannableString(currencySymbolToDisplay)

        val start = 0
        val end = currencySymbolToDisplay.length
        spannableString.setSpan(RelativeSizeSpan(.5f), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return Pair.create(spannableString, symbolAtStart)
    }

    /**
     * Returns a SpannableString representing formatted currency and shrinking the precision if the value is not whole.
     *
     */
    private fun styleCurrency(value: Double): SpannableString {
        val precision = NumberUtils.precision(value, RoundingMode.HALF_UP)
        val formattedNumber = NumberUtils.format(value.toFloat(), NumberOptions.builder().precision(precision).build())
        val spannableString = SpannableString(formattedNumber)

        if (precision != 0) {
            val startOfPrecision = formattedNumber.length - precision - 1
            val endOfPrecision = formattedNumber.length
            spannableString.setSpan(RelativeSizeSpan(.5f), startOfPrecision, endOfPrecision, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(CenterSpan(), startOfPrecision, endOfPrecision, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannableString
    }

    /**
     * Returns a CharSequence representing a value in a project's currency based on a user's locale.
     * The precision is shrunken and centered if the number is not whole.
     * The currency symbol is shrunken and centered.
     * Special case: US people looking at US currency just get the currency symbol.
     */
    fun styleCurrency(value: Double, project: Project, ksCurrency: KSCurrency): CharSequence {
        val spannedDigits = styleCurrency(value)

        val formattedCurrency = ksCurrency.format(value, project, RoundingMode.HALF_UP)

        val country = Country.findByCurrencyCode(project.currency())
            ?: return SpannableStringBuilder()

        val currencySymbolToDisplay = ksCurrency.getCurrencySymbol(country, true)

        val spannedCurrencySymbol = SpannableString(currencySymbolToDisplay)

        val startOfSymbol = 0
        val endOfSymbol = currencySymbolToDisplay.length
        spannedCurrencySymbol.setSpan(RelativeSizeSpan(.6f), startOfSymbol, endOfSymbol, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannedCurrencySymbol.setSpan(CenterSpan(), startOfSymbol, endOfSymbol, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val styledCurrency = if (formattedCurrency.startsWith(currencySymbolToDisplay.trimAllWhitespace())) {
            TextUtils.concat(spannedCurrencySymbol, spannedDigits)
        } else {
            TextUtils.concat(spannedDigits, spannedCurrencySymbol)
        }

        return styledCurrency.trim()
    }
}
