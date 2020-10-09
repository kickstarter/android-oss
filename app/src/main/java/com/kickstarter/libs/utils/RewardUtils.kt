package com.kickstarter.libs.utils

import android.content.Context
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.joda.time.DateTime
import org.joda.time.Duration
import kotlin.math.floor
import kotlin.math.max

object RewardUtils {

    /**
     * Returns `true` if the reward has backers, `false` otherwise.
     */
    fun hasBackers(reward: Reward) = IntegerUtils.isNonZero(reward.backersCount())

    fun isAvailable(project: Project, reward: Reward) = project.isLive && !isLimitReached(reward) && !isExpired(reward)

    /**
     * Returns `true` if the reward has expired.
     */
    fun isExpired(reward: Reward) = isTimeLimited(reward) && reward.endsAt()?.let {it.isBeforeNow} ?: false

    /**
     * Returns `true` if the reward has items, `false` otherwise.
     */
    fun isItemized(reward: Reward): Boolean {
        val rewardsItems = if (reward.isAddOn) reward.addOnsItems() else reward.rewardsItems()
        return rewardsItems != null && rewardsItems.isNotEmpty()
    }

    /**
     * Returns `true` if the reward has a limit set, and the limit has not been reached, `false` otherwise.
     */
    fun isLimited(reward: Reward) = reward.limit() != null && !isLimitReached(reward)

    /**
     * Returns `true` if the reward's limit has been reached, `false` otherwise.
     */
    fun isLimitReached(reward: Reward): Boolean {
        val remaining = reward.remaining()
        return reward.limit() != null && remaining != null && remaining <= 0
    }

    /**
     * Returns `true` if the reward is considered the 'non-reward' option, i.e. the reward is the option
     * backers select when they want to pledge to a project without selecting a particular reward.
     */
    fun isNoReward(reward: Reward) = reward.id() == 0L

    /**
     * Returns `true` if the reward is a specific reward for a project, i.e. it is not the 'no-reward' option.
     */
    fun isReward(reward: Reward) = !isNoReward(reward)

    /**
     * Returns `true` if the reward has shipping enabled, `false` otherwise.
     */
    fun isShippable(reward: Reward): Boolean {
        val shippingType = reward.shippingType()
        val noShippingTypes = reward.shippingPreferenceType() == Reward.ShippingPreference.NONE
        return shippingType != null && !(Reward.SHIPPING_TYPE_NO_SHIPPING == shippingType || noShippingTypes)
    }

    /**
     * Returns `true` if the reward is a Digital Reward, meaning tha it has "shippingPreference": "none"
     * if the model is a response from GraphQL or "shippingPreference": "no_shipping" if the model is
     * a response from V1
     * @param reward
     * @return isDigital: true or false
     */
    fun isDigital(reward: Reward): Boolean {
        val isDigitalV1 = reward.shippingType() != null && reward.shippingType().equals(Reward.SHIPPING_TYPE_NO_SHIPPING, ignoreCase = true)
        return (reward.shippingPreferenceType() == Reward.ShippingPreference.NONE || reward.shippingPreferenceType() == Reward.ShippingPreference.NOSHIPPING ||
                isDigitalV1) && !isShippable(reward)
    }

    /**
     * Returns `true` if the reward has a valid expiration date.
     */
    fun isTimeLimited(reward: Reward): Boolean {
//         TODO: 2019-06-14 remove epoch check after Garrow fixes `current` bug in backend
        return reward.endsAt() != null && reward.endsAt()?.let {!DateTimeUtils.isEpoch(it)} ?: false
    }

    /**
     * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
     */
    fun deadlineCountdownDetail(reward: Reward, context: Context, ksString: KSString) =
            ksString.format(
                    context.getString(R.string.discovery_baseball_card_time_left_to_go),
                    "time_left", deadlineCountdownUnit(reward, context)
            )

    /**
     * Returns the most appropriate unit for the time remaining until the reward
     * reaches its deadline.
     *
     * @param context an Android context.
     * @return the String unit.
     */
    fun deadlineCountdownUnit(reward: Reward, context: Context): String {
        val seconds = timeInSecondsUntilDeadline(reward)
        return when {
            seconds <= 1.0 && seconds > 0.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_secs)
            seconds <= 120.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_secs)
            seconds <= 120.0 * 60.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_mins)
            seconds <= 72.0 * 60.0 * 60.0 -> context.getString(R.string.discovery_baseball_card_deadline_units_hours)
            else -> context.getString(R.string.discovery_baseball_card_deadline_units_days)
        }
    }

    /**
     * Returns a Pair representing a reward's shipping summary
     * where the first value is a StringRes Integer to be used as the shipping summary
     * and the second value is a nullable String location name for rewards with single location shipping.
     *
     * Returns null for rewards that are not shippable.
     */
    fun shippingSummary(reward: Reward): Pair<Int, String?>? {
        val shippingType = reward.shippingType()
        if (!isShippable(reward) || shippingType == null) {
            return null
        }

        return when (shippingType) {
            Reward.SHIPPING_TYPE_ANYWHERE -> Pair.create(R.string.Ships_worldwide, null)
            Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS -> Pair.create(R.string.Limited_shipping, null)
            Reward.SHIPPING_TYPE_SINGLE_LOCATION -> {
                val location = reward.shippingSingleLocation()
                return location?.localizedName()?.let { Pair.create(R.string.location_name_only, it)} ?: Pair.create(R.string.Limited_shipping, "")
            }
            else -> null
        }
    }

    /**
     * Returns time until reward reaches deadline in seconds, or 0 if the
     * reward has already finished.
     */
    //TODO
    fun timeInSecondsUntilDeadline(reward: Reward) = max(0L, Duration(DateTime(), reward.endsAt()).standardSeconds)

    /**
     * Returns time remaining until reward reaches deadline in either seconds,
     * minutes, hours or days. A time unit is chosen such that the number is
     * readable, e.g. 5 minutes would be preferred to 300 seconds.
     *
     * @return the Integer time remaining.
     */
    fun deadlineCountdownValue(reward: Reward): Int {
        val seconds = timeInSecondsUntilDeadline(reward)
        return when {
            seconds <= 120.0 -> seconds.toInt()
            seconds <= 120.0 * 60.0 -> floor(seconds / 60.0).toInt()
            seconds < 72.0 * 60.0 * 60.0 -> floor(seconds / 60.0 / 60.0).toInt()
            else -> floor(seconds / 60.0 / 60.0 / 24.0).toInt()
        }
    }

    /**
     * Returns the amount value for each variant, being Control the original value, and the minimum
     * the minPledge defined by country
     *
     * @param variant the variant for which you want to get the value
     * @param reward in case no known variant as save return use the current reward.minimum amount
     * @param minPledge defined by country
     *
     * @return Double with the amount
     */
    fun rewardAmountByVariant(variant: OptimizelyExperiment.Variant?, reward: Reward, minPledge: Int): Double {
        val value =
                if (isNoReward(reward)) {
                    when (variant) {
                        OptimizelyExperiment.Variant.CONTROL -> 1.0
                        OptimizelyExperiment.Variant.VARIANT_2 -> 10.0
                        OptimizelyExperiment.Variant.VARIANT_3 -> 20.0
                        OptimizelyExperiment.Variant.VARIANT_4 -> 50.0
                        else -> reward.minimum()
                    }
                } else reward.minimum()
        return if (value < minPledge) minPledge.toDouble() else value
    }
}