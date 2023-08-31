package com.kickstarter.libs.utils

import android.content.Context
import android.util.Pair
import androidx.annotation.VisibleForTesting
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.RelativeDateTimeOptions
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.floor

object DateTimeUtils {
    /**
     * e.g.: December 2015.
     */
    @JvmOverloads
    fun estimatedDeliveryOn(date: DateTime, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat("MMMM yyyy", locale)
        return formatter.format(date.toDate())
    }

    fun isDateToday(dateTime: DateTime): Boolean {
        return (
            dateTime.withZone(DateTimeZone.UTC).withTimeAtStartOfDay()
                == DateTime.now().withTimeAtStartOfDay().withZoneRetainFields(DateTimeZone.UTC)
            )
    }

    /**
     * Returns a boolean indicating whether or not a DateTime value is the Epoch. Returns `true` if the
     * DateTime equals 1970-01-01T00:00:00Z.
     */
    fun isEpoch(dateTime: DateTime): Boolean {
        return dateTime.millis == 0L
    }

    /**
     * e.g.: Tuesday, June 20, 2017
     */
    @JvmOverloads
    fun fullDate(dateTime: DateTime, locale: Locale = Locale.getDefault()): String {
        return try {
            dateTime.toString(DateTimeFormat.fullDate().withLocale(locale).withZoneUTC())
        } catch (e: IllegalArgumentException) {
            // JodaTime doesn't support the 'cccc' pattern, triggered by fullDate and fullDateTime. See: https://github.com/dlew/joda-time-android/issues/30
            // Instead just return a medium date.
            mediumDate(dateTime, locale)
        }
    }

    /**
     * Returns the proper DateTime format pattern for supported locales.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun localePattern(locale: Locale): String {
        return when (locale.language) {
            "de" -> "MMMM yyyy"
            "en" -> "MMMM yyyy"
            "es" -> "MMMM yyyy"
            "fr" -> "MMMM yyyy"
            "ja" -> "yyyy'年'MMMM" // NB Japanese in general should show year before month
            else -> "MMMM yyyy"
        }
    }

    /**
     * e.g.: June 20, 2017
     */
    @JvmOverloads
    fun longDate(dateTime: DateTime, locale: Locale = Locale.getDefault()): String {
        return dateTime.toString(DateTimeFormat.longDate().withLocale(locale).withZoneUTC())
    }

    /**
     * e.g.: Dec 17, 2015.
     */
    @JvmOverloads
    fun mediumDate(dateTime: DateTime, locale: Locale = Locale.getDefault()): String {
        return dateTime.toString(DateTimeFormat.mediumDate().withLocale(locale).withZoneUTC())
    }

    /**
     * e.g.: Jan 14, 2016 2:20 PM.
     */
    fun mediumDateShortTime(
        dateTime: DateTime
    ): String {
        val localTime =
            Instant.ofEpochMilli(dateTime.millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

        return localTime.format(formatter)
    }

    /**
     * e.g.: Jan 14, 2016 2:20 PM EST.
     * Always uses phones timezone for the timezone string
     */
    fun mediumDateShortTimeWithTimeZone(
        dateTime: DateTime
    ): String {
        val localTime =
            Instant.ofEpochMilli(dateTime.millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        val dateTimeString = localTime.format(formatter)
        val timezoneString = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT)

        return "$dateTimeString $timezoneString"
    }

    /**
     * e.g.: Dec 17, 2015 6:35:05 PM.
     */
    @JvmOverloads
    fun mediumDateTime(
        dateTime: DateTime,
        dateTimeZone: DateTimeZone = DateTimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        return dateTime.toString(
            DateTimeFormat.mediumDateTime().withLocale(locale).withZone(dateTimeZone)
        )
    }

    /**
     * Returns a string indicating the distance between [DateTime]s. Defaults to comparing the input [DateTime] to
     * the current time.
     */
    @JvmOverloads
    fun relative(
        context: Context,
        ksString: KSString,
        dateTime: DateTime,
        options: RelativeDateTimeOptions = RelativeDateTimeOptions.builder().build()
    ): String {
        val relativeToDateTime = options.relativeToDateTime() ?: DateTime.now()
        val seconds = Seconds.secondsBetween(dateTime, relativeToDateTime)
        val secondsDifference = seconds.seconds

        if (secondsDifference >= 0.0 && secondsDifference <= 60.0) {
            return context.getString(R.string.dates_just_now)
        } else if (secondsDifference >= -60.0 && secondsDifference <= 0.0) {
            return context.getString(R.string.dates_right_now)
        }

        val unitAndDifference = unitAndDifference(
            secondsDifference,
            options.threshold()
        ) ?: // Couldn't find a good match, just render the date.
            return mediumDate(dateTime)

        val unit = unitAndDifference.first
        val difference = unitAndDifference.second
        var willHappenIn = false
        var happenedAgo = false

        if (!options.absolute()) {
            if (secondsDifference < 0) {
                willHappenIn = true
            } else if (secondsDifference > 0) {
                happenedAgo = true
            }
        }

        if (happenedAgo && "days" == unit && difference == 1) {
            return context.getString(R.string.dates_yesterday)
        }

        val baseKeyPath = StringBuilder()

        when {
            willHappenIn -> {
                baseKeyPath.append(String.format("dates_time_in_%s", unit))
            }
            happenedAgo -> {
                baseKeyPath.append(String.format("dates_time_%s_ago", unit))
            }
            else -> {
                baseKeyPath.append(String.format("dates_time_%s", unit))
            }
        }

        if (options.abbreviated()) {
            baseKeyPath.append("_abbreviated")
        }

        return ksString.format(
            baseKeyPath.toString(), difference,
            "time_count", NumberUtils.format(difference.toFloat(), NumberOptions.builder().build())
        )
    }

    /**
     * e.g.: 4:20 PM
     */
    @JvmOverloads
    fun shortTime(dateTime: DateTime, locale: Locale = Locale.getDefault()): String {
        return dateTime.toString(DateTimeFormat.shortTime().withLocale(locale).withZoneUTC())
    }

    /**
     * Utility to pair a unit (e.g. "minutes", "hours", "days") with a measurement. Returns `null` if the difference
     * exceeds the threshold.
     */
    private fun unitAndDifference(
        initialSecondsDifference: Int,
        threshold: Int
    ): Pair<String, Int>? {
        val secondsDifference = abs(initialSecondsDifference)
        val daysDifference = floor((secondsDifference / 86400).toDouble())
            .toInt()

        when {
            secondsDifference < 3600 -> { // 1 hour
                val minutesDifference = floor(secondsDifference / 60.0).toInt()
                return Pair("minutes", minutesDifference)
            }
            secondsDifference < 86400 -> { // 24 hours
                val hoursDifference = floor(secondsDifference / 60.0 / 60.0).toInt()
                return Pair("hours", hoursDifference)
            }
            secondsDifference < threshold -> {
                return Pair("days", daysDifference)
            }
            else -> return null
        }
    }
}
