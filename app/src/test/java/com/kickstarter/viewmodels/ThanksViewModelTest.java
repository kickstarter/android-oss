package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.models.OptimizelyFeature;
import com.kickstarter.libs.preferences.MockBooleanPreference;
import com.kickstarter.libs.utils.EventName;
import com.kickstarter.mock.MockExperimentsClientType;
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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
  private final TestSubscriber<Void> showSavedPromptTest = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ThanksViewModel.ViewModel(environment);
    this.vm.outputs.adapterData().subscribe(this.adapterData);
    this.vm.outputs.finish().subscribe(this.finish);
    this.vm.outputs.showGamesNewsletterDialog().subscribe(this.showGamesNewsletterDialogTest);
    this.vm.outputs.showRatingDialog().subscribe(this.showRatingDialogTest);
    this.vm.outputs.showConfirmGamesNewsletterDialog().subscribe(this.showConfirmGamesNewsletterDialogTest);
    this.vm.outputs.startDiscoveryActivity().subscribe(this.startDiscoveryTest);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectTest);
    this.vm.outputs.showSavedPrompt().subscribe(this.showSavedPromptTest);
  }

  @Test
  public void testSaveProject() {
    final Project project = ProjectFactory.project()
            .toBuilder()
            .category(CategoryFactory.artCategory())
            .build();

    setUpEnvironment(environment());

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.adapterData.assertValueCount(1);

    this.vm.inputs.onHeartButtonClicked(project);

    this.adapterData.assertValueCount(2);
    this.showSavedPromptTest.assertValueCount(1);
    this.segmentTrack.assertValues(EventName.CTA_CLICKED.getEventName());
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
      .filter(e -> "update_user_settings".equals(e.getFirst()))
      .map(e -> (User) e.getSecond().get("user"))
      .subscribe(updateUserSettingsTest);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.vm.signupToGamesNewsletterClick();
    updateUserSettingsTest.assertValues(user.toBuilder().gamesNewsletter(true).build());

    this.showConfirmGamesNewsletterDialogTest.assertValueCount(0);
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
  }

  @Test
  public void testThanksViewModel_startDiscovery() {
    setUpEnvironment(environment());
    final Category category = CategoryFactory.category();

    this.vm.inputs.categoryViewHolderClicked(category);
    this.startDiscoveryTest.assertValues(DiscoveryParams.builder().category(category).build());
  }

  @Test
  public void testThanksViewModel_startProject() {
    setUpEnvironment(environment());

    final Project project = ProjectFactory.project();
    final CheckoutData checkoutData = CheckoutDataFactory.checkoutData(3L,
            20.0, 30.0);
    final PledgeData pledgeData = PledgeData.Companion.with(PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project), RewardFactory.reward(), Collections.emptyList(), null);
    final Intent intent = new Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project);

    this.vm.intent(intent);

    this.vm.inputs.projectCardViewHolderClicked(project);

    final Pair<Project, RefTag> projectPageParams= this.startProjectTest.getOnNextEvents().get(0);
    assertEquals(projectPageParams.first, project);
    assertEquals(projectPageParams.second, RefTag.thanks());

    this.segmentTrack.assertValues(EventName.PAGE_VIEWED.getEventName(), EventName.CTA_CLICKED.getEventName());
  }

  @Test
  public void testThanksViewModel_whenFeatureFlagOn_shouldEmitProjectPage() {
    final MockCurrentUser user = new MockCurrentUser();
    final MockExperimentsClientType mockExperimentsClientType = new MockExperimentsClientType() {
      @Override
      public boolean isFeatureEnabled(final @NotNull OptimizelyFeature.Key feature) {
        return true;
      }
    };

    final Environment environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .build();

    setUpEnvironment(environment);

    final Project project = ProjectFactory.project();
    final CheckoutData checkoutData = CheckoutDataFactory.checkoutData(3L,
            20.0, 30.0);
    final PledgeData pledgeData = PledgeData.Companion.with(PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project), RewardFactory.reward(), Collections.emptyList(), null);
    final Intent intent = new Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project);

    this.vm.intent(intent);

    this.vm.inputs.projectCardViewHolderClicked(project);

    final Pair<Project, RefTag> projectPageParams= this.startProjectTest.getOnNextEvents().get(0);
    assertEquals(projectPageParams.first, project);
    assertEquals(projectPageParams.second, RefTag.thanks());

    this.segmentTrack.assertValues(EventName.PAGE_VIEWED.getEventName(), EventName.CTA_CLICKED.getEventName());
  }

  @Test
  public void testTracking_whenCheckoutDataAndPledgeDataExtrasPresent() {
    setUpEnvironment(environment());

    final Project project = ProjectFactory.project();
    final CheckoutData checkoutData = CheckoutDataFactory.checkoutData(3L,
            20.0, 30.0);
    final PledgeData pledgeData = PledgeData.Companion.with(PledgeFlowContext.NEW_PLEDGE,
            ProjectDataFactory.project(project), RewardFactory.reward(), Collections.emptyList(), null);
    final Intent intent = new Intent()
            .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
            .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            .putExtra(IntentKey.PROJECT, project);
    this.vm.intent(intent);

    this.segmentTrack.assertValue(EventName.PAGE_VIEWED.getEventName());
  }

  @Test
  public void testTracking_whenCheckoutDataAndPledgeDataExtrasNull() {
    setUpEnvironment(environment());

    final Intent intent = new Intent()
            .putExtra(IntentKey.PROJECT, ProjectFactory.project());
    this.vm.intent(intent);

    this.segmentTrack.assertNoValues();
  }
}
