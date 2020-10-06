package com.kickstarter.libs.utils;

import android.content.Context;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.models.OptimizelyExperiment;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

import androidx.annotation.NonNull;

public final class RewardUtils {
  private RewardUtils() {}

  /**
   * Returns `true` if the reward has backers, `false` otherwise.
   */
  public static boolean hasBackers(final @NonNull Reward reward) {
    return IntegerUtils.isNonZero(reward.backersCount());
  }

  public static boolean isAvailable(final @NonNull Project project, final @NonNull Reward reward) {
    return project.isLive() && !RewardUtils.isLimitReached(reward) && !RewardUtils.isExpired(reward) && hasStarted(reward);
  }

  /**
   * Returns `true` if the reward has expired.
   */
  public static boolean isExpired(final @NonNull Reward reward) {
    return isTimeLimitedEnd(reward) && reward.endsAt().isBeforeNow();
  }

  /**
   * Returns `true` if the reward has started or not limited by starting time
   * - > @return true if reward.startsAt == null
   * - > @return false if reward.startAt < now
   * - > @return true if reward.startAt >= now
   */
  public static boolean hasStarted(final @NonNull Reward reward) {
    return isTimeLimitedStart(reward) ? (!reward.startsAt().isAfterNow() || reward.startsAt().isEqualNow()) : true;
  }

  /**
   * Returns `true` if the reward has a valid expiration date on Starting date.
   */
  public static boolean isTimeLimitedStart(final @NonNull Reward reward) {
    return reward.startsAt() != null && !DateTimeUtils.isEpoch(reward.startsAt());
  }

  /**
   * Returns `true` if the reward has items, `false` otherwise.
   */
  public static boolean isItemized(final @NonNull Reward reward) {
    final List<RewardsItem> rewardsItems = reward.isAddOn()? reward.addOnsItems() : reward.rewardsItems();
    return rewardsItems != null && !rewardsItems.isEmpty();
  }

  /**
   * Returns `true` if the reward has a limit set, and the limit has not been reached, `false` otherwise.
   */
  public static boolean isLimited(final @NonNull Reward reward) {
    return reward.limit() != null && !isLimitReached(reward);
  }

  /**
   * Returns `true` if the reward's limit has been reached, `false` otherwise.
   */
  public static boolean isLimitReached(final @NonNull Reward reward) {
    final Integer remaining = reward.remaining();

    return reward.limit() != null
      && remaining != null
      && remaining <= 0;
  }

  /**
   * Returns `true` if the reward is considered the 'non-reward' option, i.e. the reward is the option
   * backers select when they want to pledge to a project without selecting a particular reward.
   */
  public static boolean isNoReward(final @NonNull Reward reward) {
    return reward.id() == 0;
  }

  /**
   * Returns `true` if the reward is a specific reward for a project, i.e. it is not the 'no-reward' option.
   */
  public static boolean isReward(final @NonNull Reward reward) {
    return !isNoReward(reward);
  }

  /**
   * Returns `true` if the reward has shipping enabled, `false` otherwise.
   */
  public static boolean isShippable(final @NonNull Reward reward) {
    final String shippingType = reward.shippingType();
    final boolean no_shipping_types = reward.shippingPreferenceType() == Reward.ShippingPreference.NONE;
    return shippingType != null && !(Reward.SHIPPING_TYPE_NO_SHIPPING.equals(shippingType) || no_shipping_types);
  }

  /**
   * Returns `true` if the reward is a Digital Reward, meaning tha it has "shippingPreference": "none"
   * if the model is a response from GraphQL or "shippingPreference": "no_shipping" if the model is
   * a response from V1
   * @param reward
   * @return isDigital: true or false
   */
  public static boolean isDigital(final @NonNull Reward reward) {
    final Boolean isDigitalV1 = reward.shippingType() != null && reward.shippingType().equalsIgnoreCase(Reward.SHIPPING_TYPE_NO_SHIPPING);

    return (reward.shippingPreferenceType() == Reward.ShippingPreference.NONE ||
            reward.shippingPreferenceType() == Reward.ShippingPreference.NOSHIPPING ||
            isDigitalV1) && !RewardUtils.isShippable(reward);
  }

  /**
   * Returns `true` if the reward has a valid expiration date on Ending date.
   */
  public static boolean isTimeLimitedEnd(final @NonNull Reward reward) {
    // TODO: 2019-06-14 remove epoch check after Garrow fixes `current` bug in backend
    return reward.endsAt() != null && !DateTimeUtils.isEpoch(reward.endsAt());
  }

  /**
   * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
   */
  public static @NonNull String deadlineCountdownDetail(final @NonNull Reward reward, final @NonNull Context context,
    final @NonNull KSString ksString) {
    return ksString.format(context.getString(R.string.discovery_baseball_card_time_left_to_go),
      "time_left", deadlineCountdownUnit(reward, context)
    );
  }

  /**
   * Returns the most appropriate unit for the time remaining until the reward
   * reaches its deadline.
   *
   * @param context an Android context.
   * @return the String unit.
   */
  public static @NonNull String deadlineCountdownUnit(final @NonNull Reward reward, final @NonNull Context context) {
    final Long seconds = timeInSecondsUntilDeadline(reward);
    if (seconds <= 1.0 && seconds > 0.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_secs);
    } else if (seconds <= 120.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_secs);
    } else if (seconds <= 120.0 * 60.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_mins);
    } else if (seconds <= 72.0 * 60.0 * 60.0) {
      return context.getString(R.string.discovery_baseball_card_deadline_units_hours);
    }
    return context.getString(R.string.discovery_baseball_card_deadline_units_days);
  }

  /**
   * Returns a Pair representing a reward's shipping summary
   * where the first value is a StringRes Integer to be used as the shipping summary
   * and the second value is a nullable String location name for rewards with single location shipping.
   * <p>
   * Returns null for rewards that are not shippable.
   */
  public static Pair<Integer, String> shippingSummary(final @NonNull Reward reward) {
    final String shippingType = reward.shippingType();

    if (!RewardUtils.isShippable(reward) || shippingType == null) {
      return null;
    }

    switch (shippingType) {
      case Reward.SHIPPING_TYPE_ANYWHERE:
        return Pair.create(R.string.Ships_worldwide, null);
      case Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS:
        return Pair.create(R.string.Limited_shipping, null);
      case Reward.SHIPPING_TYPE_SINGLE_LOCATION:
        final Reward.SingleLocation location = reward.shippingSingleLocation();
        if (ObjectUtils.isNotNull(location)) {
          return Pair.create(R.string.location_name_only, location.localizedName());
        } else {
          return Pair.create(R.string.Limited_shipping, null);
        }
    }

    return null;
  }

  /**
   * Returns time until reward reaches deadline in seconds, or 0 if the
   * reward has already finished.
   */
  public static @NonNull Long timeInSecondsUntilDeadline(final @NonNull Reward reward) {
    return Math.max(0L,
      new Duration(new DateTime(), reward.endsAt()).getStandardSeconds());
  }

  /**
   * Returns time remaining until reward reaches deadline in either seconds,
   * minutes, hours or days. A time unit is chosen such that the number is
   * readable, e.g. 5 minutes would be preferred to 300 seconds.
   *
   * @return the Integer time remaining.
   */
  public static int deadlineCountdownValue(final @NonNull Reward reward) {
    final Long seconds = timeInSecondsUntilDeadline(reward);
    if (seconds <= 120.0) {
      return seconds.intValue(); // seconds
    } else if (seconds <= 120.0 * 60.0) {
      return (int) Math.floor(seconds / 60.0); // minutes
    } else if (seconds < 72.0 * 60.0 * 60.0) {
      return (int) Math.floor(seconds / 60.0 / 60.0); // hours
    }
    return (int) Math.floor(seconds / 60.0 / 60.0 / 24.0); // days
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
  public static Double rewardAmountByVariant(final OptimizelyExperiment.Variant variant, final Reward reward, final Integer minPledge) {
    Double value = reward.minimum();

    if (isNoReward(reward)) {
      switch (variant) {
        case CONTROL:
          value = 1.0;
          break;
        case VARIANT_2:
          value = 10.0;
          break;
        case VARIANT_3:
          value = 20.0;
          break;
        case VARIANT_4:
          value = 50.0;
          break;
      }
    }

    return value < minPledge ? minPledge : value;
  }
}
