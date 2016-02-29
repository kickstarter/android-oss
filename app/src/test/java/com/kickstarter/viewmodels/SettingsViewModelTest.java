package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.User;
import com.kickstarter.ui.data.Newsletter;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class SettingsViewModelTest extends KSRobolectricTestCase {
  @Test
  public void testSettingsViewModel_sendGamesNewsletter() {
    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final SettingsViewModel vm = new SettingsViewModel(environment);

    final TestSubscriber<User> currentUserTest = new TestSubscriber<>();
    currentUser.observable().subscribe(currentUserTest);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    currentUserTest.assertValues(user);
    koalaTest.assertValues("Settings View");

    vm.inputs.sendGamesNewsletter(true);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    currentUserTest.assertValues(user, user.toBuilder().gamesNewsletter(true).build());

    vm.inputs.sendGamesNewsletter(false);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    currentUserTest.assertValues(user, user.toBuilder().gamesNewsletter(true).build(), user);

    showOptInPromptTest.assertNoValues();
  }

  @Test
  public void testSettingsViewModel_sendHappeningNewsletter() {
    final User user = UserFactory.user().toBuilder().happeningNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final SettingsViewModel vm = new SettingsViewModel(environment);

    final TestSubscriber<User> currentUserTest = new TestSubscriber<>();
    currentUser.observable().subscribe(currentUserTest);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    currentUserTest.assertValues(user);
    koalaTest.assertValues("Settings View");

    vm.inputs.sendHappeningNewsletter(true);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build());

    vm.inputs.sendHappeningNewsletter(false);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build(), user);

    showOptInPromptTest.assertNoValues();
  }

  @Test
  public void testSettingsViewModel_sendPromoNewsletter() {
    final User user = UserFactory.user().toBuilder().promoNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final SettingsViewModel vm = new SettingsViewModel(environment);

    final TestSubscriber<User> currentUserTest = new TestSubscriber<>();
    currentUser.observable().subscribe(currentUserTest);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    currentUserTest.assertValues(user);
    koalaTest.assertValues("Settings View");

    vm.inputs.sendPromoNewsletter(true);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build());

    vm.inputs.sendPromoNewsletter(false);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build(), user);

    showOptInPromptTest.assertNoValues();
  }

  @Test
  public void testSettingsViewModel_sendWeeklyNewsletter() {
    final User user = UserFactory.user().toBuilder().weeklyNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final SettingsViewModel vm = new SettingsViewModel(environment);

    final TestSubscriber<User> currentUserTest = new TestSubscriber<>();
    currentUser.observable().subscribe(currentUserTest);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    currentUserTest.assertValues(user);
    koalaTest.assertValues("Settings View");

    vm.inputs.sendWeeklyNewsletter(true);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build());

    vm.inputs.sendWeeklyNewsletter(false);
    koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build(), user);

    showOptInPromptTest.assertNoValues();
  }
}
