package com.kickstarter.mock.factories;

import com.kickstarter.libs.models.Country;
import com.kickstarter.models.Reward;

import org.joda.time.DateTime;

import java.util.Arrays;

import androidx.annotation.NonNull;

public final class RewardFactory {
  public static final DateTime ESTIMATED_DELIVERY = DateTime.parse("2019-03-26T19:26:09Z");
  private RewardFactory() {}

  public static @NonNull Reward addOn() {
    return reward().toBuilder()
            .isAddOn(true)
            .limit(10)
            .build();
  }

  public static @NonNull Reward rewardHasAddOns() {
    return reward().toBuilder()
            .hasAddons(true)
            .build();
  }

  public static @NonNull Reward reward() {
    final String description = "A digital download of the album and documentary.";
    return Reward.builder()
      .backersCount(123)
      .convertedMinimum(20.0)
      .id(IdFactory.id())
      .description(description)
      .estimatedDeliveryOn(ESTIMATED_DELIVERY)
      .minimum(20.0f)
      .shippingPreference("unrestricted")
      .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
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

  public static @NonNull Reward itemizedAddOn() {
    final long rewardId = IdFactory.id();

    return reward().toBuilder()
            .id(rewardId)
            .minimum(10)
            .isAddOn(true)
            .addOnsItems(
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

  public static @NonNull Reward multipleLocationShipping() {
    return reward().toBuilder()
      .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS)
      .estimatedDeliveryOn(ESTIMATED_DELIVERY)
      .build();
  }

  public static @NonNull Reward rewardWithShipping() {
    return reward().toBuilder()
      .shippingPreference("unrestricted")
      .shippingType(Reward.SHIPPING_TYPE_ANYWHERE)
      .estimatedDeliveryOn(ESTIMATED_DELIVERY)
      .build();
  }

  public static @NonNull Reward singleLocationShipping(final @NonNull String localizedLocationName) {
    return reward().toBuilder()
      .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
      .shippingSingleLocation(Reward.SingleLocation.builder()
        .id(IdFactory.id())
        .localizedName(localizedLocationName)
        .build())
      .estimatedDeliveryOn(ESTIMATED_DELIVERY)
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
