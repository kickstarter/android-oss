package com.kickstarter.factories;

import com.kickstarter.models.Avatar;

public class AvatarFactory {
  public static Avatar avatar() {
    final String baseUrl = "https://www.kickstarter.com/avatars/12345678/";
    return Avatar.builder()
      .medium(baseUrl + "medium.jpg")
      .small(baseUrl + "small.jpg")
      .thumb(baseUrl + "thumb.jpg")
      .build();
  }
}
