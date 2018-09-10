package com.kickstarter.mock.factories;

import com.kickstarter.models.Avatar;

public final class AvatarFactory {
  private AvatarFactory() {}

  public static Avatar avatar() {
    final String baseUrl = "https://www.kickstarter.com/avatars/12345678/";
    return Avatar.builder()
      .medium(baseUrl + "medium.jpg")
      .small(baseUrl + "small.jpg")
      .thumb(baseUrl + "thumb.jpg")
      .build();
  }
}
