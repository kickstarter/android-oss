package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.User;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class SettingsViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testSettingsViewModel_sendHappeningNewsletter() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    // Settings requires a user
    final User user = UserFactory.user().toBuilder().happeningNewsletter(false).build();
    currentUser.refresh(user);

    final SettingsViewModel vm = new SettingsViewModel(environment);
    final TestSubscriber<User> currentUserTest = new TestSubscriber<>();

    currentUser.observable().subscribe(currentUserTest);

    currentUserTest.assertValues(user);
    koalaTest.assertValues("Settings View");

    vm.inputs.sendHappeningNewsletter(true, "Happening Now");

    koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build());
  }
}
