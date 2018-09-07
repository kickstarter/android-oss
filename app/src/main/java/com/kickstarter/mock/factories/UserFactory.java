package com.kickstarter.mock.factories;

import com.kickstarter.models.User;

public final class UserFactory {
  private UserFactory() {}

  public static User user() {
    return User.builder()
      .avatar(AvatarFactory.avatar())
      .id(IdFactory.id())
      .name("Nathan Squid")
      .optedOutOfRecommendations(false)
      .location(LocationFactory.unitedStates())
      .build();
  }

  public static User socialUser() {
    return user().toBuilder().social(true).build();
  }

  public static User collaborator() {
    return user()
      .toBuilder()
      .createdProjectsCount(0)
      .memberProjectsCount(10)
      .build();
  }

  public static User creator() {
    return user()
      .toBuilder()
      .createdProjectsCount(5)
      .memberProjectsCount(10)
      .build();
  }

  public static User germanUser() {
    return user()
      .toBuilder()
      .location(LocationFactory.germany())
      .build();
  }

  public static User noRecommendations() {
    return user()
      .toBuilder()
      .optedOutOfRecommendations(true)
      .build();
  }
}
