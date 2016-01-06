package com.kickstarter.factories;

import com.kickstarter.models.Location;

public class LocationFactory {
  public static Location germany() {
    return Location.builder()
      .id(638242)
      .displayableName("Berlin, Germany")
      .name("Berlin")
      .state("Berlin")
      .country("DE")
      .build();
  }

  public static Location unitedStates() {
    return Location.builder()
      .id(12589335)
      .displayableName("Brooklyn, NY")
      .name("Brooklyn")
      .state("NY")
      .country("US")
      .build();
  }
}
