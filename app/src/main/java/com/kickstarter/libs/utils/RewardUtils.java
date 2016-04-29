package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import com.kickstarter.models.Reward;

public final class RewardUtils {
  private RewardUtils() {}

  /**
   * Returns `true` if the reward has shipping enabled, `false` otherwise.
   */
  public static boolean isShippable(final @NonNull Reward reward) {
    return BooleanUtils.isTrue(reward.shippingEnabled());
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
}
