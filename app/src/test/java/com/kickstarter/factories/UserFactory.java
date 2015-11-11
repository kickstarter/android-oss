package com.kickstarter.factories;

import com.kickstarter.models.User;

public class UserFactory {
  public static User user() {
    return User.builder()
      .avatar(AvatarFactory.avatar())
      .id(1)
      .name("Sammy Sosa")
      .id(1234567890)
      .build();
  }

  public static User creator() {
    return user();
  }
}
