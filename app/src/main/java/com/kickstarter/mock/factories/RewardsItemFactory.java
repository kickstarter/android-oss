package com.kickstarter.mock.factories;

import com.kickstarter.models.RewardsItem;

import androidx.annotation.NonNull;

public final class RewardsItemFactory {
  private RewardsItemFactory() {}

  public static @NonNull RewardsItem rewardsItem() {
    final long itemId = IdFactory.id();

    return RewardsItem.builder()
      .id(IdFactory.id())
      .item(ItemFactory.item().toBuilder().id(itemId).build())
      .itemId(itemId)
      .quantity(1)
      .rewardId(IdFactory.id())
      .build();
  }
}
