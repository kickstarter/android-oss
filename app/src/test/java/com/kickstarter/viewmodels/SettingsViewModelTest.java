package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

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
  private SettingsViewModel.ViewModel vm;
  private final TestSubscriber<User> currentUserTest = new TestSubscriber<>();
  private final TestSubscriber<Void> hideConfirmFollowingOptOutPrompt = new TestSubscriber<>();
  private final TestSubscriber<Void> showConfirmFollowingOptOutPrompt = new TestSubscriber<>();
  private final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
  private final TestSubscriber<Void> showRecommendationsInfo = new TestSubscriber<>();
  private final TestSubscriber<Void> showPrivateProfileInfo = new TestSubscriber<>();


  private void setUpEnvironment(final @NonNull User user) {
    final CurrentUserType currentUser = new MockCurrentUser(user);
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    currentUser.observable().subscribe(this.currentUserTest);

    this.vm = new SettingsViewModel.ViewModel(environment);
    this.vm.outputs.hideConfirmFollowingOptOutPrompt().subscribe(this.hideConfirmFollowingOptOutPrompt);
    this.vm.outputs.showConfirmFollowingOptOutPrompt().subscribe(this.showConfirmFollowingOptOutPrompt);
    this.vm.outputs.showRecommendationsInfo().subscribe(this.showRecommendationsInfo);
    this.vm.outputs.showPrivateProfileInfo().subscribe(this.showPrivateProfileInfo);
    this.vm.outputs.showOptInPrompt().subscribe(this.showOptInPromptTest);
  }

  @Test
  public void testSettingsViewModel_optIntoFollowing() {
    final User user = UserFactory.user();

    setUpEnvironment(user);

    this.currentUserTest.assertValues(user);

    this.vm.inputs.optIntoFollowing(true);
    this.currentUserTest.assertValues(user, user.toBuilder().social(true).build());

    this.showConfirmFollowingOptOutPrompt.assertNoValues();
    this.hideConfirmFollowingOptOutPrompt.assertNoValues();
    this.showOptInPromptTest.assertNoValues();
    this.koalaTest.assertValues("Settings View");
  }

  @Test
  public void testSettingsViewModel_optIntoFollowing_userCancelOptOut() {
    final User user = UserFactory.socialUser();

    setUpEnvironment(user);

    this.currentUserTest.assertValues(user);

    this.vm.inputs.optIntoFollowing(false);
    this.currentUserTest.assertValues(user);
    this.showConfirmFollowingOptOutPrompt.assertValueCount(1);

    this.vm.inputs.optOutOfFollowing(false);
    this.hideConfirmFollowingOptOutPrompt.assertValueCount(1);
    this.currentUserTest.assertValues(user);

    this.showOptInPromptTest.assertNoValues();
    this.koalaTest.assertValues("Settings View");
  }

  @Test
  public void testSettingsViewModel_optIntoFollowing_userConfirmOptOut() {
    final User user = UserFactory.socialUser();

    setUpEnvironment(user);

    this.currentUserTest.assertValues(user);

    this.vm.inputs.optIntoFollowing(false);
    this.currentUserTest.assertValues(user);
    this.showConfirmFollowingOptOutPrompt.assertValueCount(1);

    this.vm.inputs.optOutOfFollowing(true);
    this.currentUserTest.assertValues(user, user.toBuilder().social(false).build());

    this.hideConfirmFollowingOptOutPrompt.assertNoValues();
    this.showOptInPromptTest.assertNoValues();
    this.koalaTest.assertValues("Settings View");
  }

  @Test
  public void testSettingsViewModel_optedOutOfRecommendations() {
    final User user = UserFactory.noRecommendations();

    setUpEnvironment(user);
    
    this.currentUserTest.assertValues(user);

    this.vm.inputs.optedOutOfRecommendations(true);
    this.currentUserTest.assertValues(user, user.toBuilder().optedOutOfRecommendations(false).build());

    this.vm.inputs.optedOutOfRecommendations(false);
    this.currentUserTest.assertValues(user, user.toBuilder().optedOutOfRecommendations(false).build(), user);

    this.showOptInPromptTest.assertNoValues();
    this.koalaTest.assertValues("Settings View");
  }

  @Test
  public void testSettingsViewModel_showRecommendationsInfo() {
    final User user = UserFactory.user();

    setUpEnvironment(user);

    this.currentUserTest.assertValues(user);
    this.showRecommendationsInfo.assertValueCount(0);

    this.vm.inputs.recommendationsInfoClicked();
    this.showRecommendationsInfo.assertValueCount(1);

    this.showOptInPromptTest.assertNoValues();
    this.koalaTest.assertValues("Settings View");
  }

  @Test
  public void testSettingsViewModel_showPrivateProfileInfo() {
    final User user = UserFactory.user();

    setUpEnvironment(user);

    this.currentUserTest.assertValues(user);
    this.showPrivateProfileInfo.assertValueCount(0);

    this.vm.inputs.privateProfileInfoClicked();
    this.showPrivateProfileInfo.assertValueCount(1);

    this.showOptInPromptTest.assertNoValues();
    this.koalaTest.assertValues("Settings View");
  }

  @Test
  public void testSettingsViewModel_sendGamesNewsletter() {
    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();

    setUpEnvironment(user);

    this.vm.outputs.showOptInPrompt().subscribe(this.showOptInPromptTest);

    this.currentUserTest.assertValues(user);
    this.koalaTest.assertValues("Settings View");

    this.vm.inputs.sendGamesNewsletter(true);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().gamesNewsletter(true).build());

    this.vm.inputs.sendGamesNewsletter(false);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().gamesNewsletter(true).build(), user);

    this.showOptInPromptTest.assertNoValues();
  }

  @Test
  public void testSettingsViewModel_sendHappeningNewsletter() {
    final User user = UserFactory.user().toBuilder().happeningNewsletter(false).build();

    setUpEnvironment(user);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    this.currentUserTest.assertValues(user);
    this.koalaTest.assertValues("Settings View");

    this.vm.inputs.sendHappeningNewsletter(true);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build());

    this.vm.inputs.sendHappeningNewsletter(false);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().happeningNewsletter(true).build(), user);

    this.showOptInPromptTest.assertNoValues();
  }

  @Test
  public void testSettingsViewModel_sendPromoNewsletter() {
    final User user = UserFactory.user().toBuilder().promoNewsletter(false).build();

    setUpEnvironment(user);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    this.currentUserTest.assertValues(user);
    this.koalaTest.assertValues("Settings View");

    this.vm.inputs.sendPromoNewsletter(true);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build());

    this.vm.inputs.sendPromoNewsletter(false);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().promoNewsletter(true).build(), user);

    this.showOptInPromptTest.assertNoValues();
  }

  @Test
  public void testSettingsViewModel_sendWeeklyNewsletter() {
    final User user = UserFactory.user().toBuilder().weeklyNewsletter(false).build();

    setUpEnvironment(user);

    final TestSubscriber<Newsletter> showOptInPromptTest = new TestSubscriber<>();
    this.vm.outputs.showOptInPrompt().subscribe(showOptInPromptTest);

    this.currentUserTest.assertValues(user);
    this.koalaTest.assertValues("Settings View");

    this.vm.inputs.sendWeeklyNewsletter(true);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build());

    this.vm.inputs.sendWeeklyNewsletter(false);
    this.koalaTest.assertValues("Settings View", "Newsletter Subscribe", "Newsletter Unsubscribe");
    this.currentUserTest.assertValues(user, user.toBuilder().weeklyNewsletter(true).build(), user);

    this.showOptInPromptTest.assertNoValues();
  }
}
