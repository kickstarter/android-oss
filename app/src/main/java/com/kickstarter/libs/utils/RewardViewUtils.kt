package com.kickstarter.libs.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.joda.time.DateTime
import org.joda.time.Duration

object RewardViewUtils {

    /**
     * Returns the drawable resource ID of the check background based on project status.
     */
    @DrawableRes
    fun checkBackgroundDrawable(project: Project): Int {
        return if (project.isLive) {
            R.drawable.circle_blue_alpha_6
        } else {
            R.drawable.circle_grey_300
        }
    }

    /**
     * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
     */
    fun deadlineCountdownDetail(reward: Reward, context: Context,
                                ksString: KSString): String {
        return ksString.format(context.getString(R.string.discovery_baseball_card_time_left_to_go),
                "time_left", deadlineCountdownUnit(reward, context)
        )
    }

    /**
     * Returns the most appropriate unit for the time remaining until the reward
     * reaches its deadline.
     *
     * @param  context an Android context.
     * @return         the String unit.
     */
    fun deadlineCountdownUnit(reward: Reward, context: Context): String {
        val seconds = timeInSecondsUntilDeadline(reward)
        if (seconds <= 1.0 && seconds > 0.0) {
            return context.getString(R.string.discovery_baseball_card_deadline_units_secs)
        } else if (seconds <= 120.0) {
            return context.getString(R.string.discovery_baseball_card_deadline_units_secs)
        } else if (seconds <= 120.0 * 60.0) {
            return context.getString(R.string.discovery_baseball_card_deadline_units_mins)
        } else if (seconds <= 72.0 * 60.0 * 60.0) {
            return context.getString(R.string.discovery_baseball_card_deadline_units_hours)
        }
        return context.getString(R.string.discovery_baseball_card_deadline_units_days)
    }

    /**
     * Returns time until reward reaches deadline in seconds, or 0 if the
     * reward has already finished.
     */
    fun timeInSecondsUntilDeadline(reward: Reward): Long {
        return Math.max(0L,
                Duration(DateTime(), reward.endsAt()).standardSeconds)
    }

    /**
     * Returns time remaining until reward reaches deadline in either seconds,
     * minutes, hours or days. A time unit is chosen such that the number is
     * readable, e.g. 5 minutes would be preferred to 300 seconds.
     *
     * @return the Integer time remaining.
     */
    fun deadlineCountdownValue(reward: Reward): Int {
        val seconds = timeInSecondsUntilDeadline(reward)
        if (seconds <= 120.0) {
            return seconds.toInt() // seconds
        } else if (seconds <= 120.0 * 60.0) {
            return Math.floor(seconds / 60.0).toInt() // minutes
        } else if (seconds < 72.0 * 60.0 * 60.0) {
            return Math.floor(seconds.toDouble() / 60.0 / 60.0).toInt() // hours
        }
        return Math.floor(seconds.toDouble() / 60.0 / 60.0 / 24.0).toInt() // days
    }

    /**
     * Returns the color resource ID of the rewards button based on project and if user has backed reward.
     */
    @ColorRes
    fun pledgeButtonColor(project: Project, reward: Reward): Int {
        return if (BackingUtils.isBacked(project, reward) && project.isLive) {
            R.color.button_pledge_manage
        } else if (!project.isLive) {
            R.color.button_pledge_ended
        } else {
            R.color.button_pledge_live
        }
    }

    /**
     * Returns the string resource ID of the rewards button based on project and reward status.
     */
    @StringRes
    fun pledgeButtonAlternateText(project: Project, reward: Reward): Int {
        return if (BackingUtils.isBacked(project, reward) && project.isLive) {
            R.string.Manage_your_pledge
        } else if (BackingUtils.isBacked(project, reward) && !project.isLive) {
            R.string.View_your_pledge
        } else if (RewardUtils.isAvailable(project, reward) && project.isBacking) {
            R.string.Select_this_instead
        } else if (!RewardUtils.isAvailable(project, reward)) {
            R.string.No_longer_available
        } else {
            throw IllegalStateException()
        }
    }
}

