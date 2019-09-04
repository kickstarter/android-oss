package com.kickstarter.mock.factories;

import com.kickstarter.libs.models.Country;
import com.kickstarter.models.Reward;

import org.joda.time.DateTime;

import java.util.Arrays;

import androidx.annotation.NonNull;

public final class RewardFactory {
  private RewardFactory() {}

  public static @NonNull Reward reward() {
    final String description = "A digital download of the album and documentary.";
    return Reward.builder()
      .backersCount(123)
      .convertedMinimum(20.0)
      .id(IdFactory.id())
      .description(description)
      .estimatedDeliveryOn(DateTime.parse("2019-03-26T19:26:09Z"))
      .minimum(20.0f)
      .title("Digital Bundle")
      .build();
  }

  public static @NonNull Reward backers() {
    return reward().toBuilder()
      .backersCount(100)
      .build();
  }

  public static @NonNull Reward ended() {
    return reward().toBuilder()
      .endsAt(DateTime.now().minusDays(2))
      .build();
  }

  public static @NonNull Reward endingSoon() {
    return reward().toBuilder()
      .endsAt(DateTime.now().plusDays(2))
      .build();
  }

  public static @NonNull Reward itemized() {
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

  public static @NonNull Reward limited() {
    return reward().toBuilder()
      .limit(10)
      .remaining(5)
      .build();
  }

  public static @NonNull Reward noBackers() {
    return reward().toBuilder()
      .backersCount(0)
      .build();
  }

  public static @NonNull Reward maxReward(final @NonNull Country country) {
    return reward().toBuilder()
      .minimum(country.getMaxPledge())
      .backersCount(0)
      .build();
  }

  public static @NonNull Reward limitReached() {
    return Reward.builder()
      .backersCount(123)
      .convertedMinimum(20.0)
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
      .estimatedDeliveryOn(DateTime.parse("2019-03-26T19:26:09Z"))
      .build();
  }

  public static @NonNull Reward noReward() {
    return Reward.builder()
      .convertedMinimum(1.0)
      .id(0)
      .estimatedDeliveryOn(null)
      .description("No reward")
      .minimum(1.0f)
      .build();
  }

  public static @NonNull Reward noDescription() {
    return reward().toBuilder()
      .description("")
      .build();
  }
}
