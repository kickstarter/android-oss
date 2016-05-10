package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Reward;

import java.util.Arrays;

public final class RewardFactory {
  private RewardFactory() {}

  public static @NonNull Reward reward() {
    return Reward.builder()
      .backersCount(123)
      .id(IdFactory.id())
      .description("A digital download of the album and documentary.")
      .minimum(20.0f)
      .title("Digital Bundle")
      .build();
  }

  public static @NonNull Reward itemizedReward() {
    final long rewardId = IdFactory.id();

    return reward().toBuilder()
      .id(rewardId)
      .rewardsItems(
        Arrays.asList(
          RewardsItemFactory.rewardsItem().toBuilder()
          .rewardId(rewardId)
          .build()
        )
      )
      .build();
  }

  public static @NonNull Reward limitedReward() {
    return reward().toBuilder()
      .limit(10)
      .remaining(5)
      .build();
  }

  public static @NonNull Reward rewardWithLimitReached() {
    return Reward.builder()
      .backersCount(123)
      .id(IdFactory.id())
      .description("A digital download of the album and documentary.")
      .limit(50)
      .minimum(20.0f)
      .remaining(0)
      .title("Digital Bundle")
      .build();
  }

  public static @NonNull Reward rewardWithShipping() {
    return reward().toBuilder()
      .shippingEnabled(true)
      .shippingPreference("unrestricted")
      .shippingSummary("Ships anywhere in the world")
      .build();
  }

  public static @NonNull Reward noReward() {
    return Reward.builder()
      .id(0)
      .description("No reward")
      .minimum(1.0f)
      .build();
  }
}
