package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.mock.factories.InternalBuildEnvelopeFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import rx.observers.TestSubscriber;

public class DiscoveryViewModelTest extends KSRobolectricTestCase {
  private DiscoveryViewModel.ViewModel vm;
  private final TestSubscriber<List<Integer>> clearPages = new TestSubscriber<>();
  private final TestSubscriber<Boolean> creatorDashboardButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> drawerIsOpen = new TestSubscriber<>();
  private final TestSubscriber<Boolean> expandSortTabLayout = new TestSubscriber<>();
  private final TestSubscriber<Void> navigationDrawerDataEmitted = new TestSubscriber<>();
  private final TestSubscriber<Integer> position = new TestSubscriber<>();
  private final TestSubscriber<List<Category>> rootCategories = new TestSubscriber<>();
  private final TestSubscriber<Boolean> rotatedExpandSortTabLayout = new TestSubscriber<>();
  private final TestSubscriber<Integer> rotatedUpdatePage = new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> rotatedUpdateParams= new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> rotatedUpdateToolbarWithParams = new TestSubscriber<>();
  private final TestSubscriber<InternalBuildEnvelope> showBuildCheckAlert = new TestSubscriber<>();
  private final TestSubscriber<Void> showInternalTools = new TestSubscriber<>();
  private final TestSubscriber<Void> showLoginTout = new TestSubscriber<>();
  private final TestSubscriber<Void> showProfile = new TestSubscriber<>();
  private final TestSubscriber<Void> showSettings = new TestSubscriber<>();
  private final TestSubscriber<Integer> updatePage = new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> updateParams= new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> updateToolbarWithParams = new TestSubscriber<>();

  @Test
  public void testBuildCheck() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());
    final InternalBuildEnvelope buildEnvelope = InternalBuildEnvelopeFactory.newerBuildAvailable();

    this.vm.outputs.showBuildCheckAlert().subscribe(this.showBuildCheckAlert);

    // Build check should not be shown.
    this.showBuildCheckAlert.assertNoValues();

    // Build check should be shown when newer build is available.
    this.vm.inputs.newerBuildIsAvailable(buildEnvelope);
    this.showBuildCheckAlert.assertValue(buildEnvelope);
  }

  @Test
  public void testCreatorDashboardButtonIsGone_isTrue_WhenCreatorOrCollaborator() {
    final User notCreator = UserFactory.user().toBuilder().memberProjectsCount(0).build();
    final MockCurrentUser currentUser = new MockCurrentUser(notCreator);

    final Environment env = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    this.vm = new DiscoveryViewModel.ViewModel(env);
    this.vm.outputs.creatorDashboardButtonIsGone().subscribe(this.creatorDashboardButtonIsGone);
    this.creatorDashboardButtonIsGone.assertValue(true);
  }

  @Test
  public void testCreatorDashboardButtonIsGone_isFalse_WhenCreator() {
    final User creator = UserFactory.creator();
    final MockCurrentUser currentUser = new MockCurrentUser(creator);

    final Environment env = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    this.vm = new DiscoveryViewModel.ViewModel(env);
    this.vm.outputs.creatorDashboardButtonIsGone().subscribe(this.creatorDashboardButtonIsGone);
    this.creatorDashboardButtonIsGone.assertValue(false);
  }

  @Test
  public void testCreatorDashboardButtonIsGone_isFalse_WhenCollaborator() {
    final User collaborator = UserFactory.collaborator();
    final MockCurrentUser currentUser = new MockCurrentUser(collaborator);

    final Environment env = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    this.vm = new DiscoveryViewModel.ViewModel(env);
    this.vm.outputs.creatorDashboardButtonIsGone().subscribe(this.creatorDashboardButtonIsGone);
    this.creatorDashboardButtonIsGone.assertValue(false);
  }

  @Test
  public void testDrawerData() {
    final MockCurrentUser currentUser = new MockCurrentUser();
    final Environment env = environment().toBuilder().currentUser(currentUser).build();
    this.vm = new DiscoveryViewModel.ViewModel(env);

    this.vm.outputs.navigationDrawerData().compose(Transformers.ignoreValues()).subscribe(this.navigationDrawerDataEmitted);
    this.vm.outputs.drawerIsOpen().subscribe(this.drawerIsOpen);

    // Initialize activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Drawer data should emit. Drawer should be closed.
    this.navigationDrawerDataEmitted.assertValueCount(1);
    this.drawerIsOpen.assertNoValues();
    this.koalaTest.assertNoValues();

    // Open drawer and click the top PWL filter.
    this.vm.inputs.openDrawer(true);
    this.vm.inputs.topFilterViewHolderRowClick(null, NavigationDrawerData.Section.Row
      .builder()
      .params(DiscoveryParams.builder().staffPicks(true).build())
      .build()
    );

    // Drawer data should emit. Drawer should open, then close upon selection.
    this.navigationDrawerDataEmitted.assertValueCount(2);
    this.drawerIsOpen.assertValues(true, false);
    this.koalaTest.assertValues("Discover Switch Modal", "Discover Modal Selected Filter");

    // Open drawer and click a child filter.
    this.vm.inputs.openDrawer(true);
    this.vm.inputs.childFilterViewHolderRowClick(null, NavigationDrawerData.Section.Row
      .builder()
      .params(DiscoveryParams
        .builder()
        .category(CategoryFactory.artCategory())
        .build()
      )
      .build()
    );

    // Drawer data should emit. Drawer should open, then close upon selection.
    this.navigationDrawerDataEmitted.assertValueCount(3);
    this.drawerIsOpen.assertValues(true, false, true, false);
    this.koalaTest.assertValues("Discover Switch Modal", "Discover Modal Selected Filter", "Discover Switch Modal",
      "Discover Modal Selected Filter");
  }

  @Test
  public void testUpdateInterfaceElementsWithParams() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.outputs.updateToolbarWithParams().subscribe(this.updateToolbarWithParams);
    this.vm.outputs.expandSortTabLayout().subscribe(this.expandSortTabLayout);

    // Initialize activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial HOME page selected.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 0);

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true);

    // Toolbar params should be loaded with initial params.
    this.updateToolbarWithParams.assertValues(DiscoveryParams.builder().build());

    // Select POPULAR sort.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 1);

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true);

    // Unchanged toolbar params should not emit.
    this.updateToolbarWithParams.assertValues(DiscoveryParams.builder().build());

    // Select ALL PROJECTS filter from drawer.
    this.vm.inputs.topFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder().params(DiscoveryParams.builder().build()).build()
    );

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true, true);
    this.koalaTest.assertValues("Discover Modal Selected Filter");

    // Select ART category from drawer.
    this.vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true, true, true);
    this.koalaTest.assertValues("Discover Modal Selected Filter", "Discover Modal Selected Filter");

    // Simulate rotating the device and hitting initial inputs again.
    this.vm.outputs.updateToolbarWithParams().subscribe(this.rotatedUpdateToolbarWithParams);
    this.vm.outputs.expandSortTabLayout().subscribe(this.rotatedExpandSortTabLayout);

    // Simulate recreating and setting POPULAR fragment, the previous position before rotation.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 1);

    // Sort tab and toolbar params should emit again with same params.
    this.rotatedExpandSortTabLayout.assertValues(true);
    this.rotatedUpdateToolbarWithParams.assertValues(
      DiscoveryParams.builder().category(CategoryFactory.artCategory()).build()
    );
  }

  @Test
  public void testClickingInterfaceElements() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.outputs.showInternalTools().subscribe(this.showInternalTools);
    this.vm.outputs.showLoginTout().subscribe(this.showLoginTout);
    this.vm.outputs.showProfile().subscribe(this.showProfile);
    this.vm.outputs.showSettings().subscribe(this.showSettings);

    this.showInternalTools.assertNoValues();
    this.showLoginTout.assertNoValues();
    this.showProfile.assertNoValues();
    this.showSettings.assertNoValues();

    this.vm.inputs.loggedInViewHolderInternalToolsClick(null);
    this.vm.inputs.loggedOutViewHolderLoginToutClick(null);
    this.vm.inputs.loggedInViewHolderProfileClick(null, UserFactory.user());
    this.vm.inputs.loggedInViewHolderSettingsClick(null, UserFactory.user());

    this.showInternalTools.assertValueCount(1);
    this.showLoginTout.assertValueCount(1);
    this.showProfile.assertValueCount(1);
    this.showSettings.assertValueCount(1);
  }

  @Test
  public void testInteractionBetweenParamsAndPageAdapter() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.outputs.updateParamsForPage().subscribe(this.updateParams);
    this.vm.outputs.updateParamsForPage().map(params -> DiscoveryUtils.positionFromSort(params.sort())).subscribe(this.updatePage);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial HOME page selected.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 0);

    // Initial params should emit. Page should not be updated yet.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build()
    );
    this.updatePage.assertValues(0);

    // Select POPULAR sort position.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 1);

    // Params and page should update with new POPULAR sort values.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()
    );
    this.updatePage.assertValues(0, 1);

    // Select ART category from the drawer.
    this.vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Params should update with new category; page should remain the same.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(CategoryFactory.artCategory()).build()
    );
    this.updatePage.assertValues(0, 1, 1);
    this.koalaTest.assertValues("Discover Modal Selected Filter");

    // Select HOME sort position.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 0);

    // Params and page should update with new HOME sort value.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(CategoryFactory.artCategory()).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).category(CategoryFactory.artCategory()).build()
    );
    this.updatePage.assertValues(0, 1, 1, 0);

    // Simulate rotating the device and hitting initial inputs again.
    this.vm.outputs.updateParamsForPage().subscribe(this.rotatedUpdateParams);
    this.vm.outputs.updateParamsForPage().map(params -> DiscoveryUtils.positionFromSort(params.sort())).subscribe(this.rotatedUpdatePage);

    // Should emit again with same params.
    this.rotatedUpdateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).category(CategoryFactory.artCategory()).build()
    );
    this.rotatedUpdatePage.assertValues(0);
  }

  @Test
  public void testDefaultParams_withUserLoggedOut() {
    setUpDefaultParamsTest(null);

    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build()
    );
  }

  @Test
  public void testDefaultParams_withUserLoggedIn_optedIn() {
    setUpDefaultParamsTest(UserFactory.user());

    this.updateParams.assertValues(
      DiscoveryParams.builder().recommended(true).backed(-1).sort(DiscoveryParams.Sort.HOME).build()
    );
  }

  @Test
  public void testDefaultParams_withUserLoggedIn_optedOut() {
    setUpDefaultParamsTest(UserFactory.noRecommendations());

    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build()
    );
  }

  @Test
  public void testClearingPages() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.outputs.clearPages().subscribe(this.clearPages);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    this.clearPages.assertNoValues();

    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 1);

    this.clearPages.assertNoValues();

    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 3);

    this.clearPages.assertNoValues();

    // Select ART category from the drawer.
    this.vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    this.clearPages.assertValues(Arrays.asList(0, 1, 2));

    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 1);

    // Select MUSIC category from the drawer.
    this.vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.musicCategory()).build())
        .build()
    );

    this.clearPages.assertValues(Arrays.asList(0, 1, 2), Arrays.asList(0, 2, 3));
  }

  @Test
  public void testRootCategoriesEmitWithPosition() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.outputs.rootCategoriesAndPosition().map(cp -> cp.first).subscribe(this.rootCategories);
    this.vm.outputs.rootCategoriesAndPosition().map(cp -> cp.second).subscribe(this.position);

    // Start initial activity.
    this.vm.intent(new Intent(Intent.ACTION_MAIN));

    // Initial HOME page selected.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 0);

    // Root categories should emit for the initial HOME sort this.position.
    this.rootCategories.assertValueCount(1);
    this.position.assertValues(0);

    // Select POPULAR sort position.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 1);

    // Root categories should emit for the POPULAR sort position.
    this.rootCategories.assertValueCount(2);
    this.position.assertValues(0, 1);

    // Select ART category from the drawer.
    this.vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Root categories should not emit again for the same position.
    this.rootCategories.assertValueCount(2);
    this.position.assertValues(0, 1);
  }

  private void setUpDefaultParamsTest(final @Nullable User user) {
    final Environment.Builder environmentBuilder = environment().toBuilder();

    if (user != null) {
      final MockCurrentUser currentUser = new MockCurrentUser(user);
      environmentBuilder.currentUser(currentUser);
    }

    this.vm = new DiscoveryViewModel.ViewModel(environmentBuilder.build());
    this.vm.outputs.updateParamsForPage().subscribe(this.updateParams);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial HOME page selected.
    this.vm.inputs.discoveryPagerAdapterSetPrimaryPage(null, 0);
  }
}
