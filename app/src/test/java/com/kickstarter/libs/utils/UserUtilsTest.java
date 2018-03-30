package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.models.User;

import org.junit.Test;

public class UserUtilsTest extends KSRobolectricTestCase {

  @Test
  public void testUserHasChanged() {
    final User user15 = UserFactory.user().toBuilder().id(15).build();
    final User user21 = UserFactory.user().toBuilder().id(21).build();

    assertTrue(UserUtils.userHasChanged(null, UserFactory.user()));
    assertTrue(UserUtils.userHasChanged(UserFactory.user(), null));
    assertTrue(UserUtils.userHasChanged(user15, user21));
    assertFalse(UserUtils.userHasChanged(null, null));
    assertFalse(UserUtils.userHasChanged(user15, user15));
  }
}
