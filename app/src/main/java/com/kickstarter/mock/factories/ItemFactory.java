package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Item;

public final class ItemFactory {
  private ItemFactory() {}

  public static @NonNull Item item() {
    return Item.builder()
      .amount(10.0f)
      .id(IdFactory.id())
      .name("T-Shirt")
      .projectId(IdFactory.id())
      .build();
  }
}
