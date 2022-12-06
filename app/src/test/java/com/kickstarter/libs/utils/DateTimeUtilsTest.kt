package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RelativeDateTimeOptions.Companion.builder
import com.kickstarter.libs.utils.DateTimeUtils.relative
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.Locale

class DateTimeUtilsTest : KSRobolectricTestCase() {

    @Before
    fun init() {
        // -  DateTimeZone.forID("EST")) requires initializing joda time library, on newest versions the initializing method has been deprecated look for an alternative
        JodaTimeAndroid.init(context())
    }

    @Test
    fun testEstimatedDeliveryOn() {
        assertEquals(
            "December 2015",
            DateTimeUtils.estimatedDeliveryOn(DateTime.parse("2015-12-17T18:35:05Z"), Locale.getDefault())
        )
        assertEquals(
            "décembre 2015",
            DateTimeUtils.estimatedDeliveryOn(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH)
        )
        assertEquals(
            "12月 2015",
            DateTimeUtils.estimatedDeliveryOn(
                DateTime.parse("2015-12-17T18:35:05Z"),
                Locale.JAPANESE
            )
        )
    }

    @Test
    fun testLocalPattern() {
        assertEquals(
            "yyyy'年'MMMM",
            DateTimeUtils.localePattern(Locale.JAPANESE)
        )
        assertEquals(
            "MMMM yyyy",
            DateTimeUtils.localePattern(Locale.FRENCH)
        )

        assertEquals(
            "MMMM yyyy",
            DateTimeUtils.localePattern(Locale.GERMANY)
        )

        assertEquals(
            "MMMM yyyy",
            DateTimeUtils.localePattern(Locale("es"))
        )

        assertEquals(
            "MMMM yyyy",
            DateTimeUtils.localePattern(Locale.KOREA)
        )
    }

    @Test
    fun testFullDate() {
        assertEquals(
            "Thursday, December 17, 2015",
            DateTimeUtils.fullDate(DateTime.parse("2015-12-17T18:35:05Z"))
        )
        assertEquals(
            "jeudi 17 décembre 2015",
            DateTimeUtils.fullDate(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH)
        )
    }

    @Test
    fun testIsEpoch() {
        assertTrue(DateTimeUtils.isEpoch(DateTime.parse("1970-01-01T00:00:00Z")))
        assertTrue(DateTimeUtils.isEpoch(DateTime.parse("1969-12-31T19:00:00.000-05:00")))
        assertFalse(DateTimeUtils.isEpoch(DateTime.parse("2015-12-17T18:35:05Z")))
    }

    @Test
    fun testLongDate() {
        assertEquals(
            "December 17, 2015",
            DateTimeUtils.longDate(DateTime.parse("2015-12-17T18:35:05Z"))
        )
        assertEquals(
            "17 décembre 2015",
            DateTimeUtils.longDate(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH)
        )
    }

    @Test
    fun testMediumDate() {
        assertEquals(
            "Dec 17, 2015",
            DateTimeUtils.mediumDate(DateTime.parse("2015-12-17T18:35:05Z"))
        )
        assertEquals(
            "17 déc. 2015",
            DateTimeUtils.mediumDate(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH)
        )
    }

    @Test
    fun testMediumDateTime() {
        assertEquals(
            "Dec 17, 2015, 6:35:05 PM",
            DateTimeUtils.mediumDateTime(DateTime.parse("2015-12-17T18:35:05Z"))
        )

        assertEquals(
            "Dec 17, 2015, 6:35:05 PM",
            DateTimeUtils.mediumDateTime(DateTime.parse("2015-12-17T18:35:05Z"), DateTimeZone.UTC)
        )
        assertEquals(
            "Dec 17, 2015, 1:35:05 PM",
            DateTimeUtils.mediumDateTime(
                DateTime.parse("2015-12-17T18:35:05Z"),
                DateTimeZone.forID("EST")
            )
        )
        assertEquals(
            "17 déc. 2015 à 18:35:05",
            DateTimeUtils.mediumDateTime(
                DateTime.parse("2015-12-17T18:35:05Z"),
                DateTimeZone.UTC,
                Locale.FRENCH
            )
        )
    }

    @Test
    fun testRelative() {
        val context = context()
        val ksString = ksString()
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val builder = builder()

        assertEquals(
            "just now",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:10Z")).build()
            )
        )
        assertEquals(
            "right now",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T18:35:00Z")).build()
            )
        )
        assertEquals(
            "2 minutes ago",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()
            )
        )
        assertEquals(
            "in 2 minutes",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()
            )
        )
        assertEquals(
            "1 hour ago",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T19:35:05Z")).build()
            )
        )
        assertEquals(
            "in 1 hour",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T17:35:05Z")).build()
            )
        )
        assertEquals(
            "4 hours ago",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()
            )
        )
        assertEquals(
            "in 4 hours",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()
            )
        )
        assertEquals(
            "23 hours ago",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-18T17:35:05Z")).build()
            )
        )
        assertEquals(
            "in 23 hours",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-16T19:35:05Z")).build()
            )
        )
        assertEquals(
            "yesterday",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-18T18:35:05Z")).build()
            )
        )
        assertEquals(
            "in 1 day",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-16T18:35:05Z")).build()
            )
        )
        assertEquals(
            "10 days ago",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-27T18:35:05Z")).build()
            )
        )
        assertEquals(
            "in 10 days",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-07T18:35:05Z")).build()
            )
        )
        assertEquals(
            "Dec 17, 2015",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2016-01-27T18:35:05Z")).build()
            )
        )
        assertEquals(
            "Dec 17, 2015",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-10-17T18:35:05Z")).build()
            )
        )

        assertEquals(
            "Dec 17, 2015",
            relative(
                context, ksString, dateTime,
                builder.build()
            )
        )

        assertEquals(
            "Dec 17, 2015",
            relative(context, ksString, dateTime)
        )
    }

    @Test
    fun testRelative_withAbbreviated() {
        val context = context()
        val ksString = ksString()
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val builder = builder().abbreviated(true)

        assertEquals(
            "4 hrs ago",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()
            )
        )
        assertEquals(
            "in 4 hrs",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()
            )
        )
    }

    @Test
    fun testRelative_withAbsolute() {
        val context = context()
        val ksString = ksString()
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val builder = builder().absolute(true)

        assertEquals(
            "4 hours",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T22:35:05Z")).build()
            )
        )
        assertEquals(
            "4 hours",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T14:35:05Z")).build()
            )
        )
    }

    @Test
    fun testRelative_withThreshold() {
        val context = context()
        val ksString = ksString()
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val threshold = 864000 // Ten days
        val relativeDateTimeOptions = builder().threshold(threshold)

        assertEquals(
            "9 days ago",
            DateTimeUtils.relative(
                context, ksString, dateTime,
                relativeDateTimeOptions.relativeToDateTime(
                    DateTime.parse("2015-12-26T18:35:05Z")
                ).build()
            )
        )

        assertEquals(
            "in 9 days",
            DateTimeUtils.relative(
                context, ksString, dateTime,
                relativeDateTimeOptions.relativeToDateTime(DateTime.parse("2015-12-08T18:35:05Z")).build()
            )
        )

        assertEquals(
            "Dec 17, 2015",
            DateTimeUtils.relative(
                context, ksString, dateTime,
                relativeDateTimeOptions.relativeToDateTime(DateTime.parse("2015-12-28T18:35:05Z")).build()
            )
        )

        assertEquals(
            "Dec 17, 2015",
            DateTimeUtils.relative(
                context, ksString, dateTime,
                relativeDateTimeOptions.relativeToDateTime(DateTime.parse("2015-12-06T18:35:05Z")).build()
            )
        )
    }

    @Test
    @Config(qualifiers = "de")
    fun testRelative_withLocale() {
        val context = context()
        val ksString = ksString()
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val builder = builder()

        assertEquals(
            "vor 2 Minuten",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T18:37:05Z")).build()
            )
        )
        assertEquals(
            "in 2 Minuten",
            relative(
                context, ksString, dateTime,
                builder.relativeToDateTime(DateTime.parse("2015-12-17T18:33:05Z")).build()
            )
        )
    }

    @Test
    fun testMediumDateShortTime() {
        assertEquals(
            "Dec 17, 2015, 6:35 PM",
            DateTimeUtils.mediumDateShortTime(
                DateTime.parse("2015-12-17T18:35:05Z")
            )
        )

        assertEquals(
            "Dec 17, 2015, 6:35 PM",
            DateTimeUtils.mediumDateShortTime(
                DateTime.parse("2015-12-17T18:35:05Z"),
                DateTimeZone.UTC
            )
        )
        assertEquals(
            "Dec 17, 2015, 1:35 PM",
            DateTimeUtils.mediumDateShortTime(
                DateTime.parse("2015-12-17T18:35:05Z"),
                DateTimeZone.forID("EST")
            )
        )
        assertEquals(
            "17 déc. 2015 18:35",
            DateTimeUtils.mediumDateShortTime(
                DateTime.parse("2015-12-17T18:35:05Z"),
                DateTimeZone.UTC,
                Locale.FRENCH
            )
        )
    }

    @Test
    fun testShortTime() {
        assertEquals("6:35 PM", DateTimeUtils.shortTime(DateTime.parse("2015-12-17T18:35:05Z")))
        assertEquals(
            "18:35",
            DateTimeUtils.shortTime(DateTime.parse("2015-12-17T18:35:05Z"), Locale.FRENCH)
        )
    }
}
