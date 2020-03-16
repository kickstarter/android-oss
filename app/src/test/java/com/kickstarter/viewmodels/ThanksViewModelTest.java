package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.preferences.MockBooleanPreference;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.mock.factories.CheckoutDataFactory;
import com.kickstarter.mock.factories.LocationFactory;
import com.kickstarter.mock.factories.ProjectDataFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.data.ThanksData;
import com.kickstarter.ui.data.CheckoutData;
import com.kickstarter.ui.data.PledgeData;
import com.kickstarter.ui.data.PledgeFlowContext;

import org.junit.Test;

import java.util.Arrays;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public final class ThanksViewModelTest extends KSRobolectricTestCase {
  private ThanksViewModel.ViewModel vm;
  private final TestSubscriber<ThanksData> adapterData = new TestSubscriber<>();
  private final TestSubscriber<Void> finish = new TestSubscriber<>();
  private final TestSubscriber<Void> showGamesNewsletterDialogTest = new TestSubscriber<>();
  private final TestSubscriber<Void> showRatingDialogTest = new TestSubscriber<>();
  private final TestSubscriber<Void> showConfirmGamesNewsletterDialogTest = TestSubscriber.create();
  private final TestSubscriber<DiscoveryParams> startDiscoveryTest = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectTest = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ThanksViewModel.ViewModel(environment);
    this.vm.outputs.adapterData().subscribe(this.adapterData);
    this.vm.outputs.finish().subscribe(this.finish);
    this.vm.outputs.showGamesNewsletterDialog().subscribe(this.showGamesNewsletterDialogTest);
    this.vm.outputs.showRatingDialog().subscribe(this.showRatingDialogTest);
    this.vm.outputs.showConfirmGamesNewsletterDialog().subscribe(this.showConfirmGamesNewsletterDialogTest);
    this.vm.outputs.startDiscoveryActivity().subscribe(this.startDiscoveryTest);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectTest);
  }

  @Test
  public void testThanksViewModel_adapterData() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.artCategory())
      .build();

    setUpEnvironment(environment());

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.adapterData.assertValueCount(1);
  }

  @Test
  public void testFinishEmits() {
    setUpEnvironment(environment());

    final Intent intent = new Intent()
      .putExtra(IntentKey.PROJECT, ProjectFactory.project());
    this.vm.intent(intent);
    this.vm.inputs.closeButtonClicked();

    this.finish.assertValueCount(1);
  }

  @Test
  public void testThanksViewModel_showRatingDialog() {
    final MockBooleanPreference hasSeenAppRatingPreference = new MockBooleanPreference(false);
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(true);

    final Environment environment = environment()
      .toBuilder()
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));
    this.showRatingDialogTest.assertValueCount(1);
  }

  @Test
  public void testThanksViewModel_dontShowRatingDialogIfAlreadySeen() {
    final MockBooleanPreference hasSeenAppRatingPreference = new MockBooleanPreference(true);
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(true);

    final Environment environment = environment()
      .toBuilder()
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));
    this.showRatingDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_dontShowRatingDialogIfGamesNewsletterWillDisplay() {
    final MockBooleanPreference hasSeenAppRatingPreference = new MockBooleanPreference(false);
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(false);

    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);
    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.showRatingDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_showGamesNewsletterDialog() {
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(false);

    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.showGamesNewsletterDialogTest.assertValueCount(1);
    assertEquals(Arrays.asList(false, true), hasSeenGamesNewsletterPreference.values());
    this.koalaTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_dontShowGamesNewsletterDialogIfRootCategoryIsNotGames() {
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(false);
    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.ceramicsCategory())
      .build();

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.showGamesNewsletterDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_dontShowGamesNewsletterDialogIfUserHasAlreadySeen() {
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(true);
    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.showGamesNewsletterDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_dontShowGamesNewsletterDialogIfUserHasAlreadySignedUp() {
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(false);
    final User user = UserFactory.user().toBuilder().gamesNewsletter(true).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    setUpEnvironment(environment);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.showGamesNewsletterDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_signupToGamesNewsletterOnClick() {
    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);

    final TestSubscriber<User> updateUserSettingsTest = new TestSubscriber<>();
    ((MockApiClient) environment.apiClient()).observable()
      .filter(e -> "update_user_settings".equals(e.first))
      .map(e -> (User) e.second.get("user"))
      .subscribe(updateUserSettingsTest);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.vm.signupToGamesNewsletterClick();
    updateUserSettingsTest.assertValues(user.toBuilder().gamesNewsletter(true).build());

    this.showConfirmGamesNewsletterDialogTest.assertValueCount(0);
    this.koalaTest.assertValues("Newsletter Subscribe");
  }

  @Test
  public void testThanksViewModel_showNewsletterConfirmationPromptAfterSignupForGermanUser() {
    final User user = UserFactory.user().toBuilder()
      .gamesNewsletter(false)
      .location(LocationFactory.germany())
      .build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);

    final Project project = ProjectFactory.project().toBuilder().category(CategoryFactory.tabletopGamesCategory()).build();
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.signupToGamesNewsletterClick();
    this.showConfirmGamesNewsletterDialogTest.assertValueCount(1);
    this.koalaTest.assertValues("Newsletter Subscribe");
  }

  @Test
  public void testThanksViewModel_startDiscovery() {
    setUpEnvironment(environment());
    final Category category = CategoryFactory.category();

    this.vm.inputs.categoryViewHolderClicked(category);
    this.startDiscoveryTest.assertValues(DiscoveryParams.builder().category(category).build());
    this.koalaTest.assertValue("Checkout Finished Discover More");
  }

  @Test
  public void testThanksViewModel_startProject() {
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project();

    this.vm.inputs.projectCardViewHolderClicked(project);
    this.startProjectTest.assertValues(Pair.create(project, RefTag.thanks()));
    this.koalaTest.assertValue("Checkout Finished Discover Open Project");
  }

  @Test
  public void testTracking_whenCheckoutDataAndPledgeDataExtrasExist() {
    setUpEnvironment(environment());

    final Project project = ProjectFactory.project();
    final CheckoutData checkoutData = CheckoutDataFactory.Companion.checkoutData(3L,
            20.0, 30.0);
    final PledgeData pledgeData = PledgeData.Companion.with(PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.Companion.project(project), RewardFactory.reward());
    final Intent intent = new Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project);
    this.vm.intent(intent);

    this.lakeTest.assertValue("Thanks Page Viewed");
  }

  @Test
  public void testTracking_whenCheckoutDataAndPledgeDataExtrasPresent() {
    setUpEnvironment(environment());

    final Intent intent = new Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project());
    this.vm.intent(intent);

    this.lakeTest.assertNoValues();
  }
}
