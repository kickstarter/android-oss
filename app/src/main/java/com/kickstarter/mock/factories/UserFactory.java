package com.kickstarter.mock.factories;

import com.kickstarter.models.User;

public final class UserFactory {
  private UserFactory() {}

  public static User user() {
    return User.builder()
      .avatar(AvatarFactory.avatar())
      .id(IdFactory.id())
      .isEmailVerified(true)
      .name("Some Name")
      .optedOutOfRecommendations(false)
      .location(LocationFactory.unitedStates())
      .build();
  }

  public static User userNotVerifiedEmail() {
    return User.builder()
            .avatar(AvatarFactory.avatar())
            .id(IdFactory.id())
            .isEmailVerified(false)
            .name("Some Name")
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

  public static User canadianUser() {
    return user()
            .toBuilder()
            .location(LocationFactory.germany())
            .build();
  }

  public static User mexicanUser() {
    return user()
            .toBuilder()
            .location(LocationFactory.mexico())
            .build();
  }

  public static User noRecommendations() {
    return user()
      .toBuilder()
      .optedOutOfRecommendations(true)
      .build();
  }

  public static User allTraitsTrue() {
    return UserFactory.user().toBuilder()
            .id(1)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfMarketingUpdate(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build();
  }
}
