package com.kickstarter.libs.utils

import android.content.Context
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.RewardUtils.deadlineCountdownDetail
import com.kickstarter.libs.utils.RewardUtils.deadlineCountdownUnit
import com.kickstarter.libs.utils.RewardUtils.deadlineCountdownValue
import com.kickstarter.libs.utils.RewardUtils.hasBackers
import com.kickstarter.libs.utils.RewardUtils.isAvailable
import com.kickstarter.libs.utils.RewardUtils.isExpired
import com.kickstarter.libs.utils.RewardUtils.isItemized
import com.kickstarter.libs.utils.RewardUtils.isLimitReached
import com.kickstarter.libs.utils.RewardUtils.isLimited
import com.kickstarter.libs.utils.RewardUtils.isNoReward
import com.kickstarter.libs.utils.RewardUtils.isReward
import com.kickstarter.libs.utils.RewardUtils.isShippable
import com.kickstarter.libs.utils.RewardUtils.isTimeLimited
import com.kickstarter.libs.utils.RewardUtils.rewardAmountByVariant
import com.kickstarter.libs.utils.RewardUtils.shippingSummary
import com.kickstarter.libs.utils.RewardUtils.timeInSecondsUntilDeadline
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Reward
import org.joda.time.DateTime
import org.joda.time.MutableDateTime
import org.junit.Before
import org.junit.Test
import java.util.Date

class RewardUtilsTest : KSRobolectricTestCase() {

    private val DAYS_TO_GO = "days to go"
    private val HOURS_TO_GO = "hours to go"
    private val SECS_TO_GO = "secs to go"
    private val DAYS = "days"
    private val HOURS = "hours"
    private val MINS = "mins"
    private val SECS = "secs"
    private val COUNTRY_NIGERIA = "Nigeria"

    private lateinit var context : Context
    private lateinit var ksString : KSString
    private lateinit var date : Date
    private lateinit var currentDate : MutableDateTime
    private lateinit var reward: Reward
    private lateinit var noReward: Reward
    private lateinit var rewardEnded: Reward
    private lateinit var rewardLimitReached: Reward
    private lateinit var rewardMultipleShippingLocation: Reward
    private lateinit var rewardWorldWideShipping: Reward
    private lateinit var rewardSingleShippingLocation: Reward

    @Before
    fun setUpTests() {
        context = context()
        ksString = ksString()
        date = DateTime.now().toDate()
        currentDate = MutableDateTime(date)
        noReward = RewardFactory.noReward()
        reward = RewardFactory.reward()
        rewardEnded = RewardFactory.ended()
        rewardLimitReached = RewardFactory.limitReached()
        rewardMultipleShippingLocation = RewardFactory.multipleLocationShipping()
        rewardWorldWideShipping = RewardFactory.rewardWithShipping()
        rewardSingleShippingLocation = RewardFactory.singleLocationShipping(LocationFactory.nigeria().displayableName())
    }

    @Test
    fun testIsAvailable() {
        assertTrue(isAvailable(ProjectFactory.project(), reward))
        assertFalse(isAvailable(ProjectFactory.project(), rewardEnded))
        assertFalse(isAvailable(ProjectFactory.project(), rewardLimitReached))
        assertFalse(isAvailable(ProjectFactory.successfulProject(), reward))
        assertFalse(isAvailable(ProjectFactory.successfulProject(), rewardEnded))
        assertFalse(isAvailable(ProjectFactory.successfulProject(), rewardLimitReached))
    }

    @Test
    fun deadlineCountdownDetail_whenDaysLeft_returnsDayToGoString() {
        currentDate.addDays(31)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownDetail(reward, context, ksString), DAYS_TO_GO)
    }

    @Test
    fun deadlineCountdownDetail_whenHoursLeft_returnsHoursToGoString() {
        currentDate.addHours(3)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownDetail(reward, context, ksString), HOURS_TO_GO)
    }

    @Test
    fun deadlineCountdownDetail_whenSecondsLeft_returnsSecondsToGoString() {
        currentDate.addSeconds(3)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownDetail(reward, context, ksString), SECS_TO_GO)
    }

    @Test
    fun deadlineCountdownUnit_whenDaysLeft_returnsDays() {
        currentDate.addDays(31)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownUnit(reward, context), DAYS)
    }

    @Test
    fun deadlineCountdownUnit_whenHoursLeft_returnsHours() {
        currentDate.addHours(3)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownUnit(reward, context), HOURS)
    }

    @Test
    fun deadlineCountdownUnit_whenMinutesLeft_returnsMinutes() {
        currentDate.addMinutes(3)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownUnit(reward, context), MINS)
    }

    @Test
    fun deadlineCountdownUnit_whenSecondsLeft_returnsSecs() {
        currentDate.addSeconds(30)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownUnit(reward, context), SECS)
    }

    @Test
    fun deadlineCountdownValue_whenMinutesLeft_returnsNumberOfMinutes() {
        currentDate.addSeconds(300)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownValue(reward), 5)
    }

    @Test
    fun deadlineCountdownValue_whenHoursLeft_returnsNumberOfHours() {
        currentDate.addSeconds(3600)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownValue(reward), 60)
    }

    @Test
    fun deadlineCountdownValue_whenDaysLeft_returnsNumberOfDays() {
        currentDate.addSeconds(86400)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownValue(reward), 24)
    }

    @Test
    fun deadlineCountdownValue_whenSecondsLeft_returnsNumberOfSeconds() {
        currentDate.addSeconds(30)
        reward = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(deadlineCountdownValue(reward), 30)
    }

    @Test
    fun testHasBackers() {
        assertTrue(hasBackers(RewardFactory.backers()))
        assertFalse(hasBackers(RewardFactory.noBackers()))
    }

    @Test
    fun isLimited_whenRemainingGreaterThanZeroAndLimitNotNull_returnsTrue() {
        reward = RewardFactory.reward().toBuilder()
                .remaining(5)
                .limit(10)
                .build()
        assertTrue(isLimited(reward))
    }

        @Test
        fun isLimited_whenRemainingZeroAndLimitNotNull_returnFalse() {
            reward = RewardFactory.reward().toBuilder()
                    .remaining(0)
                    .limit(10)
                    .build()
            assertFalse(isLimited(reward))
        }

        @Test
        fun isLimited_whenLimitAndRemainingNull_returnFalse() {
            reward = RewardFactory.reward().toBuilder()
                    .remaining(null)
                    .limit(null)
                    .build()
            assertFalse(isLimited(reward))
        }

    @Test
    fun testIsItemized() {
        assertFalse(isItemized(reward))
        assertTrue(isItemized(RewardFactory.itemized()))
        assertTrue(isItemized(RewardFactory.itemizedAddOn()))
    }

    @Test
    fun issLimitReached_whenLimitSetAndRemainingIsZero_returnTrue() {
        reward = RewardFactory.reward().toBuilder()
                .limit(100)
                .remaining(0)
                .build()
        assertTrue(isLimitReached(reward))
    }

    @Test
    fun isLimitReached_whenLimitSetButRemainingIsNull_returnFalse() {
        reward = RewardFactory.reward().toBuilder()
                .limit(100)
                .build()
        assertFalse(isLimitReached(reward))
    }

    @Test
    fun isLimitReached_whenRemainingIsGreaterThanZero_returnFalse() {
        reward = RewardFactory.reward().toBuilder()
                .limit(100)
                .remaining(50)
                .build()
        assertFalse(isLimitReached(reward))
    }

    @Test
    fun testIsReward() {
        assertTrue(isReward(reward))
        assertFalse(isReward(noReward))
    }

    @Test
    fun testIsNoReward() {
        assertTrue(isNoReward(noReward))
        assertFalse(isNoReward(reward))
    }

    @Test
    fun testIsShippable() {
        val rewardWithNullShipping = RewardFactory.reward()
                .toBuilder()
                .shippingType(null)
                .build()
        assertFalse(isShippable(rewardWithNullShipping))
        assertFalse(isShippable(reward))
        assertTrue(isShippable(rewardMultipleShippingLocation))
        assertTrue(isShippable(rewardSingleShippingLocation))
        assertTrue(isShippable(rewardWorldWideShipping))
    }

    @Test
    fun isTimeLimited() {
        assertFalse(isTimeLimited(reward))
        assertTrue(isTimeLimited(RewardFactory.endingSoon()))
    }

    @Test
    fun isExpired_whenEndsAtPastDate_returnsTrue() {
        assertFalse(isExpired(reward))
        reward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().minusDays(2))
                .build()
        assertTrue(isExpired(reward))
    }

    @Test
    fun isExpired_whenEndsAtFutureDate_returnsFalse() {
        reward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().plusDays(2))
                .build()
        assertFalse(isExpired(reward))
    }

    @Test
    fun testShippingSummary() {
        val rewardWithNullShipping = RewardFactory.reward()
                .toBuilder()
                .shippingType(null)
                .build()
        assertNull(shippingSummary(rewardWithNullShipping))
        assertNull(shippingSummary(reward))
        assertEquals(Pair.create<Int, Any?>(R.string.Limited_shipping, null), shippingSummary(rewardMultipleShippingLocation))
        assertEquals(Pair.create(R.string.location_name_only, COUNTRY_NIGERIA), shippingSummary(rewardSingleShippingLocation))
        assertEquals(Pair.create<Int, Any?>(R.string.Ships_worldwide, null), shippingSummary(rewardWorldWideShipping))
    }

    @Test
    fun minimumRewardAmountByVariant() {
        assertEquals(1.0, rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, noReward, 1))
        assertEquals(10.0, rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_2, noReward, 1))
        assertEquals(20.0, rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_3, noReward, 1))
        assertEquals(50.0, rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_4, noReward, 1))
        assertEquals(10.0, rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, noReward, 10))
        assertEquals(100.0, rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_4, noReward, 100))
    }

    @Test
    fun testTimeInSecondsUntilDeadline() {
        currentDate.addSeconds(120)
        reward = RewardFactory.reward().toBuilder().endsAt(currentDate.toDateTime()).build()
        val timeInSecondsUntilDeadline = timeInSecondsUntilDeadline(reward)
        assertEquals(timeInSecondsUntilDeadline, 120)
    }
}