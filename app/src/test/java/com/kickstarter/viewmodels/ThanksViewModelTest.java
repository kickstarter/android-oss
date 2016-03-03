package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.preferences.MockBooleanPreference;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

public final class ThanksViewModelTest extends KSRobolectricTestCase {
  @Test
  public void testThanksViewModel_projectName() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> projectNameTest = new TestSubscriber<>();
    vm.outputs.projectName().subscribe(projectNameTest);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    projectNameTest.assertValues(project.name());
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

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showRatingDialogTest = new TestSubscriber<>();
    vm.outputs.showRatingDialog().subscribe(showRatingDialogTest);

    final Project project = ProjectFactory.project();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showRatingDialogTest.assertValueCount(1);
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

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showRatingDialogTest = new TestSubscriber<>();
    vm.outputs.showRatingDialog().subscribe(showRatingDialogTest);

    final Project project = ProjectFactory.project();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showRatingDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_dontShowRatingDialogIfGamesNewsletterWillDisplay() {
    final MockBooleanPreference hasSeenAppRatingPreference = new MockBooleanPreference(false);
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(false);
    final TestSubscriber<Boolean> hasSeenGamesNewsletterPreferenceTest = new TestSubscriber<>();
    hasSeenGamesNewsletterPreference.observable().subscribe(hasSeenGamesNewsletterPreferenceTest);

    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showRatingDialogTest = new TestSubscriber<>();
    vm.outputs.showRatingDialog().subscribe(showRatingDialogTest);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showRatingDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_showGamesNewsletterDialog() {
    final MockBooleanPreference hasSeenGamesNewsletterPreference = new MockBooleanPreference(false);
    final TestSubscriber<Boolean> hasSeenGamesNewsletterPreferenceTest = new TestSubscriber<>();
    hasSeenGamesNewsletterPreference.observable().subscribe(hasSeenGamesNewsletterPreferenceTest);

    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .build();

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showGamesNewsletterDialogTest = new TestSubscriber<>();
    vm.outputs.showGamesNewsletterDialog().subscribe(showGamesNewsletterDialogTest);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showGamesNewsletterDialogTest.assertValueCount(1);
    hasSeenGamesNewsletterPreferenceTest.assertValues(false, true);
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

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showGamesNewsletterDialogTest = new TestSubscriber<>();
    vm.outputs.showGamesNewsletterDialog().subscribe(showGamesNewsletterDialogTest);


    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.ceramicsCategory())
      .build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showGamesNewsletterDialogTest.assertValueCount(0);
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

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showGamesNewsletterDialogTest = new TestSubscriber<>();
    vm.outputs.showGamesNewsletterDialog().subscribe(showGamesNewsletterDialogTest);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showGamesNewsletterDialogTest.assertValueCount(0);
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

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showGamesNewsletterDialogTest = new TestSubscriber<>();
    vm.outputs.showGamesNewsletterDialog().subscribe(showGamesNewsletterDialogTest);

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(CategoryFactory.tabletopGamesCategory())
      .build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showGamesNewsletterDialogTest.assertValueCount(0);
  }

  @Test
  public void testThanksViewModel_showRecommendations() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Pair<List<Project>, Category>> showRecommendationsTest = new TestSubscriber<>();
    vm.outputs.showRecommendations().subscribe(showRecommendationsTest);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    showRecommendationsTest.assertValueCount(1);
  }

  @Test
  public void testThanksViewModel_share() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> startShareTest = new TestSubscriber<>();
    vm.outputs.startShare().subscribe(startShareTest);
    final TestSubscriber<Project> startShareOnFacebookTest = new TestSubscriber<>();
    vm.outputs.startShareOnFacebook().subscribe(startShareOnFacebookTest);
    final TestSubscriber<Project> startShareOnTwitterTest = new TestSubscriber<>();
    vm.outputs.startShareOnTwitter().subscribe(startShareOnTwitterTest);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));

    vm.inputs.shareClick();
    startShareTest.assertValues(project);
    koalaTest.assertValues("Checkout Show Share Sheet");

    vm.inputs.shareOnFacebookClick();
    startShareOnFacebookTest.assertValues(project);
    koalaTest.assertValues("Checkout Show Share Sheet", "Checkout Show Share");

    vm.inputs.shareOnTwitterClick();
    startShareOnTwitterTest.assertValues(project);
    koalaTest.assertValues("Checkout Show Share Sheet", "Checkout Show Share", "Checkout Show Share");
  }

  @Test
  public void testThanksViewModel_signupToGamesNewsletterOnClick() {
    final User user = UserFactory.user().toBuilder().gamesNewsletter(false).build();
    final CurrentUserType currentUser = new MockCurrentUser(user);

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<User> updateUserSettingsTest = new TestSubscriber<>();
    ((MockApiClient) environment.apiClient()).observable()
      .filter(e -> "update_user_settings".equals(e.first))
      .map(e -> (User) e.second.get("user"))
      .subscribe(updateUserSettingsTest);

    final TestSubscriber<Void> showConfirmGamesNewsletterDialogTest = TestSubscriber.create();
    vm.outputs.showConfirmGamesNewsletterDialog().subscribe(showConfirmGamesNewsletterDialogTest);

    final Project project = ProjectFactory.project().toBuilder().category(CategoryFactory.tabletopGamesCategory()).build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    vm.signupToGamesNewsletterClick();
    updateUserSettingsTest.assertValues(user.toBuilder().gamesNewsletter(true).build());
    showConfirmGamesNewsletterDialogTest.assertValueCount(0);
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

    final ThanksViewModel vm = new ThanksViewModel(environment);

    final TestSubscriber<Void> showConfirmGamesNewsletterDialogTest = TestSubscriber.create();
    vm.outputs.showConfirmGamesNewsletterDialog().subscribe(showConfirmGamesNewsletterDialogTest);

    final Project project = ProjectFactory.project().toBuilder().category(CategoryFactory.tabletopGamesCategory()).build();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    vm.signupToGamesNewsletterClick();
    showConfirmGamesNewsletterDialogTest.assertValueCount(1);
  }

  @Test
  public void testThanksViewModel_startDiscovery() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Category category = CategoryFactory.category();

    final TestSubscriber<DiscoveryParams> startDiscoveryTest = new TestSubscriber<>();
    vm.outputs.startDiscovery().subscribe(startDiscoveryTest);

    vm.inputs.categoryClick(null, category);
    startDiscoveryTest.assertValues(DiscoveryParams.builder().category(category).build());

    koalaTest.assertValue("Checkout Finished Discover More");
  }

  @Test
  public void testThanksViewModel_startProject() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> startProjectTest = new TestSubscriber<>();
    vm.outputs.startProject().subscribe(startProjectTest);

    vm.inputs.projectClick(null, project);
    startProjectTest.assertValues(project);

    koalaTest.assertValue("Checkout Finished Discover Open Project");
  }
}
