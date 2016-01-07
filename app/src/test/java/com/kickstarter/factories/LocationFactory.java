package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Location;

public final class LocationFactory {
  private LocationFactory() {}

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
}
