package com.kickstarter.mock.factories;

import com.kickstarter.models.Location;

import androidx.annotation.NonNull;

public final class LocationFactory {
  private LocationFactory() {}

  public static @NonNull Location germany() {
    return Location.builder()
      .id(638242)
      .displayableName("Berlin, Germany")
      .name("Berlin")
      .state("Berlin")
      .country("DE")
      .expandedCountry("Germany")
      .build();
  }

  public static @NonNull Location mexico() {
    return Location.builder()
      .id(638242)
      .displayableName("Mexico City, Mexico")
      .name("Mexico City")
      .state("Mexico")
      .country("MX")
      .expandedCountry("Mexico")
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
      .expandedCountry("Australia")
      .build();
  }

  public static @NonNull Location unitedStates() {
    return Location.builder()
      .id(12589335)
      .displayableName("Brooklyn, NY")
      .name("Brooklyn")
      .state("NY")
      .country("US")
      .expandedCountry("United States")
      .build();
  }
}
