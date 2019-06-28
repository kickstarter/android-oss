package com.kickstarter.libs.utils;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class RewardUtils {
  private RewardUtils() {}

  public static final double MAX_REWARD_LIMIT = 2000000000;

  /**
   * Returns `true` if the reward has backers, `false` otherwise.
   */
  public static boolean hasBackers(final @NonNull Reward reward) {
    return IntegerUtils.isNonZero(reward.backersCount());
  }

  public static boolean isAvailable(final @NonNull Project project, final @NonNull Reward reward) {
    return project.isLive() && !RewardUtils.isLimitReached(reward) && !RewardUtils.isExpired(reward);
  }

  /**
   * Returns `true` if the reward has expired.
   */
  public static boolean isExpired(final @NonNull Reward reward) {
    return isTimeLimited(reward) && reward.endsAt().isBeforeNow();
  }

  /**
   * Returns `true` if the reward has items, `false` otherwise.
   */
  public static boolean isItemized(final @NonNull Reward reward) {
    final List<RewardsItem> rewardsItems = reward.rewardsItems();
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

  public static boolean isMaxRewardAmount(final double amount) {
    return amount >= MAX_REWARD_LIMIT;
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
    return isTrue(reward.shippingEnabled());
  }

  /**
   * Returns `true` if the reward has a valid expiration date.
   */
  public static boolean isTimeLimited(final @NonNull Reward reward) {
    // TODO: 2019-06-14 remove epoch check after Garrow fixes `current` bug in backend
    return reward.endsAt() != null && !DateTimeUtils.isEpoch(reward.endsAt());
  }

}
