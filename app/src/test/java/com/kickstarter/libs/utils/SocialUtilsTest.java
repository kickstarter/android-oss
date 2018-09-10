package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.User;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SocialUtilsTest extends KSRobolectricTestCase {
  @Test
  public void testProjectCardNamepile_oneFriend() {
    final List<User> friends = Collections.singletonList(UserFactory.user().toBuilder().name("Anna").build());
    assertEquals("Anna is a backer.", SocialUtils.projectCardFriendNamepile(context(), friends, ksString()));
  }

  @Test
  public void testProjectCardNamepile_twoFriends() {
    final List<User> friends = Arrays.asList(
      UserFactory.user().toBuilder().name("Anna").build(),
      UserFactory.user().toBuilder().name("Ben").build()
    );
    assertEquals("Anna and Ben are backers.", SocialUtils.projectCardFriendNamepile(context(), friends, ksString()));
  }

  @Test
  public void testProjectCardNamepile_threeFriends() {
    final List<User> friends = Arrays.asList(
      UserFactory.user().toBuilder().name("Anna").build(),
      UserFactory.user().toBuilder().name("Ben").build(),
      UserFactory.user().toBuilder().name("Charles").build()
    );
    assertEquals("Anna, Ben, and Charles are backers.", SocialUtils.projectCardFriendNamepile(context(), friends, ksString()));
  }

  @Test
  public void testProjectCardNamepile_fourFriends() {
    final List<User> friends = Arrays.asList(
      UserFactory.user().toBuilder().name("Anna").build(),
      UserFactory.user().toBuilder().name("Ben").build(),
      UserFactory.user().toBuilder().name("Charles").build(),
      UserFactory.user().toBuilder().name("Darlene").build()
    );
    assertEquals("Anna, Ben, Charles, and 1 more are backers.", SocialUtils.projectCardFriendNamepile(context(), friends, ksString()));
  }
}

