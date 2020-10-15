package com.kickstarter.mock.factories;

import com.kickstarter.models.Location;

import androidx.annotation.NonNull;

public final class LocationFactory {
  private LocationFactory() {}

  public static @NonNull Location germany() {
    return Location.builder()
      .id(1L)
      .displayableName("Berlin, Germany")
      .name("Berlin")
      .state("Berlin")
      .country("DE")
      .expandedCountry("Germany")
      .build();
  }

  public static @NonNull Location mexico() {
    return Location.builder()
      .id(2L)
      .displayableName("Mexico City, Mexico")
      .name("Mexico City")
      .state("Mexico")
      .country("MX")
      .expandedCountry("Mexico")
      .build();
  }

  public static @NonNull Location nigeria() {
    return Location.builder()
      .id(3L)
      .displayableName("Nigeria")
      .name("Nigeria")
      .state("Imo State")
      .country("NG")
      .expandedCountry("Nigeria")
      .build();
  }

  public static @NonNull Location sydney() {
    return Location.builder()
      .id(4L)
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
      .id(5L)
      .displayableName("Brooklyn, NY")
      .name("Brooklyn")
      .state("NY")
      .country("US")
      .expandedCountry("United States")
      .build();
  }

  public static @NonNull Location empty() {
    return Location.builder()
            .id(-1L)
            .displayableName("")
            .name("")
            .country("")
            .expandedCountry("")
            .build();
  }
}
