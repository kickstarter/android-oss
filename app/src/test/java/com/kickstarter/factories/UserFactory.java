package com.kickstarter.factories;

import com.kickstarter.models.User;

public class UserFactory {
  public static User user() {
    User user = new User();
    user.id = 1;
    user.uid = "1234567890";

    return user;
  }

  public static User creator() {
    User user = user();

    return user;
  }
}
