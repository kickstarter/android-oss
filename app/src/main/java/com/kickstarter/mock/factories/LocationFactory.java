package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Location;

public final class LocationFactory {
  private LocationFactory() {}

  public static @NonNull Location germany() {
    return Location.builder()
      .id(638242)
      .displayableName("Berlin, Germany")
      .name("Berlin")
      .state("Berlin")
      .country("DE")
      .build();
  }

  public static @NonNull Location sydney() {
    return Location.builder()
      .id(1105779)
      .name("Sydney")
      .displayableName("Sydney, AU")
      .country("AU")
      .state("NSW")
      .projectsCount(33)
      .build();
  }

  public static @NonNull Location unitedStates() {
    return Location.builder()
      .id(12589335)
      .displayableName("Brooklyn, NY")
      .name("Brooklyn")
      .state("NY")
      .country("US")
      .build();
  }
}
