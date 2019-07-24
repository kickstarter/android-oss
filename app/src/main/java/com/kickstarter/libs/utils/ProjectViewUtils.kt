package com.kickstarter.libs.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Pair
import android.view.View
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.models.Country
import com.kickstarter.models.Project
import com.kickstarter.ui.views.CenterSpan
import java.math.RoundingMode

object ProjectViewUtils {

    /**
     * Returns the color resource ID of the rewards button based on project and backing status.
     */
    @ColorRes
    fun rewardsButtonColor(project: Project): Int {
        return if (project.isBacking && project.isLive) {
            R.color.button_pledge_manage
        } else if (!project.isLive) {
            R.color.button_pledge_ended
        } else {
            R.color.button_pledge_live
        }
    }

    fun rewardsButtonText(project: Project): Int {
        return if (!project.isBacking && project.isLive) {
            R.string.Back_this_project
        } else if (project.isBacking && project.isLive) {
            R.string.Manage
        } else if (project.isBacking && !project.isLive) {
            R.string.View_your_pledge
        } else {
            R.string.View_rewards
        }
    }

    @StringRes
    fun rewardsToolbarTitle(project: Project): Int {
        return if (!project.isBacking && project.isLive) {
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
     * Set correct button view based on project and backing status.
     */
    @JvmStatic
    fun setActionButton(project: Project, backProjectButton: Button,
                        managePledgeButton: Button, viewPledgeButton: Button,
                        viewRewardsButton: Button?) {

        if (!project.isBacking && project.isLive) {
            backProjectButton.visibility = View.VISIBLE
        } else {
            backProjectButton.visibility = View.GONE
        }

        if (project.isBacking && project.isLive) {
            managePledgeButton.visibility = View.VISIBLE
        } else {
            managePledgeButton.visibility = View.GONE
        }

        if (project.isBacking && !project.isLive) {
            viewPledgeButton.visibility = View.VISIBLE
        } else {
            viewPledgeButton.visibility = View.GONE
        }

        viewRewardsButton?.let {
            if (!project.isBacking && !project.isLive) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    /**
     * Returns a SpannableString representing formatted currency and shrinking the precision if the value is not whole.
     *
     */
    fun styleCurrency(value: Double): SpannableString {
        val precision = NumberUtils.precision(value, RoundingMode.HALF_UP)
        val formattedNumber = NumberUtils.format(value.toFloat(), NumberOptions.builder().precision(precision).build())
        val spannableString = SpannableString(formattedNumber)

        if (precision != 0) {
            val startOfPrecision = formattedNumber.length - precision - 1
            val endOfPrecision = formattedNumber.length
            spannableString.setSpan(RelativeSizeSpan(.5f), startOfPrecision, endOfPrecision, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spannableString.setSpan(CenterSpan(), startOfPrecision, endOfPrecision, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        return spannableString
    }

    /**
     * Returns a Pair of SpannableString ans a Boolean where the
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
        if (formattedCurrency.endsWith(StringUtils.trim(currencySymbolToDisplay))) {
            symbolAtStart = false
        }

        val spannableString = SpannableString(currencySymbolToDisplay)

        val start = 0
        val end = currencySymbolToDisplay.length
        spannableString.setSpan(RelativeSizeSpan(.5f), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return Pair.create(spannableString, symbolAtStart)
    }
}
