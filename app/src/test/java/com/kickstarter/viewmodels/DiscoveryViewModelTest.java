package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.InternalBuildEnvelopeFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.observers.TestSubscriber;

public class DiscoveryViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testBuildCheck() {
    final DiscoveryViewModel vm = new DiscoveryViewModel(environment());
    final InternalBuildEnvelope buildEnvelope = InternalBuildEnvelopeFactory.newerBuildAvailable();

    final TestSubscriber<InternalBuildEnvelope> showBuildCheckAlert = new TestSubscriber<>();
    vm.outputs.showBuildCheckAlert().subscribe(showBuildCheckAlert);

    // Build check should not be shown.
    showBuildCheckAlert.assertNoValues();

    // Build check should be shown when newer build is available.
    vm.inputs.newerBuildIsAvailable(buildEnvelope);
    showBuildCheckAlert.assertValue(buildEnvelope);
  }

  @Test
  public void testDrawerData() {
    final MockCurrentUser currentUser = new MockCurrentUser();
    final Environment env = environment().toBuilder().currentUser(currentUser).build();
    final DiscoveryViewModel vm = new DiscoveryViewModel(env);

    final TestSubscriber<Void> navigationDrawerDataEmitted = new TestSubscriber<>();
    vm.outputs.navigationDrawerData().compose(Transformers.ignoreValues()).subscribe(navigationDrawerDataEmitted);

    final TestSubscriber<Boolean> drawerIsOpen = new TestSubscriber<>();
    vm.outputs.drawerIsOpen().subscribe(drawerIsOpen);

    // Initialize activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    vm.intent(intent);

    // Drawer data should emit. Drawer should be closed.
    navigationDrawerDataEmitted.assertValueCount(1);
    drawerIsOpen.assertNoValues();
    koalaTest.assertNoValues();

    // Open drawer and click the top PWL filter.
    vm.inputs.openDrawer(true);
    vm.inputs.topFilterViewHolderRowClick(null, NavigationDrawerData.Section.Row
      .builder()
      .params(DiscoveryParams.builder().staffPicks(true).build())
      .build()
    );

    // Drawer data should emit. Drawer should open, then close upon selection.
    navigationDrawerDataEmitted.assertValueCount(2);
    drawerIsOpen.assertValues(true, false);
    koalaTest.assertValues("Discover Switch Modal", "Discover Modal Selected Filter");

    // Open drawer and click a child filter.
    vm.inputs.openDrawer(true);
    vm.inputs.childFilterViewHolderRowClick(null, NavigationDrawerData.Section.Row
      .builder()
      .params(DiscoveryParams
        .builder()
        .category(CategoryFactory.artCategory())
        .build()
      )
      .build()
    );

    // Drawer data should emit. Drawer should open, then close upon selection.
    navigationDrawerDataEmitted.assertValueCount(3);
    drawerIsOpen.assertValues(true, false, true, false);
    koalaTest.assertValues("Discover Switch Modal", "Discover Modal Selected Filter", "Discover Switch Modal",
      "Discover Modal Selected Filter");
  }

  @Test
  public void testUpdateInterfaceElementsWithParams() {
    final DiscoveryViewModel vm = new DiscoveryViewModel(environment());

    final TestSubscriber<DiscoveryParams> updateToolbarWithParams = new TestSubscriber<>();
    vm.outputs.updateToolbarWithParams().subscribe(updateToolbarWithParams);

    final TestSubscriber<Boolean> expandSortTabLayout = new TestSubscriber<>();
    vm.outputs.expandSortTabLayout().subscribe(expandSortTabLayout);

    // Initialize activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    vm.intent(intent);

    // Notify activity that pager adapter has created fragment pages.
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 0);
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 1);

    // Sort tab should be expanded.
    expandSortTabLayout.assertValues(true);

    // Toolbar params should be loaded with initial params.
    updateToolbarWithParams.assertValues(DiscoveryParams.builder().build());

    // Select POPULAR sort.
    vm.inputs.pageChanged(1);

    // Sort tab should be expanded.
    expandSortTabLayout.assertValues(true, true);

    // Unchanged toolbar params should not emit.
    updateToolbarWithParams.assertValues(DiscoveryParams.builder().build());

    // Select ALL PROJECTS filter from drawer.
    vm.inputs.topFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder().params(DiscoveryParams.builder().build()).build()
    );

    // Sort tab should be expanded.
    expandSortTabLayout.assertValues(true, true, true);
    koalaTest.assertValues("Discover Modal Selected Filter");

    // Select ART category from drawer.
    vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Sort tab should be expanded.
    expandSortTabLayout.assertValues(true, true, true, true);
    koalaTest.assertValues("Discover Modal Selected Filter", "Discover Modal Selected Filter");

    // Simulate rotating the device and hitting initial inputs again.
    final TestSubscriber<DiscoveryParams> rotatedUpdateToolbarWithParams = new TestSubscriber<>();
    vm.outputs.updateToolbarWithParams().subscribe(rotatedUpdateToolbarWithParams);
    final TestSubscriber<Boolean> rotatedExpandSortTabLayout = new TestSubscriber<>();
    vm.outputs.expandSortTabLayout().subscribe(rotatedExpandSortTabLayout);

    // Simulate recreating current and next fragment.
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 0);
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 1);

    // Sort tab and toolbar params should emit again with same params.
    rotatedExpandSortTabLayout.assertValues(true);
    rotatedUpdateToolbarWithParams.assertValues(
      DiscoveryParams.builder().category(CategoryFactory.artCategory()).build()
    );
  }

  @Test
  public void testClickingInterfaceElements() {
    final DiscoveryViewModel vm = new DiscoveryViewModel(environment());

    final TestSubscriber<Void> showInternalTools = new TestSubscriber<>();
    vm.outputs.showInternalTools().subscribe(showInternalTools);
    final TestSubscriber<Void> showLoginTout = new TestSubscriber<>();
    vm.outputs.showLoginTout().subscribe(showLoginTout);
    final TestSubscriber<Void> showProfile = new TestSubscriber<>();
    vm.outputs.showProfile().subscribe(showProfile);
    final TestSubscriber<Void> showSettings = new TestSubscriber<>();
    vm.outputs.showSettings().subscribe(showSettings);

    showInternalTools.assertNoValues();
    showLoginTout.assertNoValues();
    showProfile.assertNoValues();
    showSettings.assertNoValues();

    vm.inputs.loggedInViewHolderInternalToolsClick(null);
    vm.inputs.loggedOutViewHolderLoginToutClick(null);
    vm.inputs.loggedInViewHolderProfileClick(null, UserFactory.user());
    vm.inputs.loggedInViewHolderSettingsClick(null, UserFactory.user());

    showInternalTools.assertValueCount(1);
    showLoginTout.assertValueCount(1);
    showProfile.assertValueCount(1);
    showSettings.assertValueCount(1);
  }

  @Test
  public void testInteractionBetweenParamsAndPageAdapter() {
    final DiscoveryViewModel vm = new DiscoveryViewModel(environment());

    final TestSubscriber<DiscoveryParams> updateParams= new TestSubscriber<>();
    vm.outputs.updateParamsForPage().map(pair -> pair.first).subscribe(updateParams);
    final TestSubscriber<Integer> updatePage = new TestSubscriber<>();
    vm.outputs.updateParamsForPage().map(pair -> pair.second).subscribe(updatePage);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    vm.intent(intent);

    // Notify activity when pager adapter pages are created.
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 0);
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 1);

    // Initial params should emit. Page should not be updated yet.
    updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build()
    );
    updatePage.assertValues(0);

    // Select POPULAR sort position.
    vm.inputs.pageChanged(1);

    // Params and page should update with new POPULAR sort values.
    updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()
    );
    updatePage.assertValues(0, 1);

    // Select ART category from the drawer.
    vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Params should update with new category; page should remain the same.
    updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(CategoryFactory.artCategory()).build()
    );
    updatePage.assertValues(0, 1, 1);
    koalaTest.assertValues("Discover Modal Selected Filter");

    // Select HOME sort position.
    vm.inputs.pageChanged(0);

    // Params and page should update with new HOME sort value.
    updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(CategoryFactory.artCategory()).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).category(CategoryFactory.artCategory()).build()
    );
    updatePage.assertValues(0, 1, 1, 0);

    // Simulate rotating the device and hitting initial inputs again.
    final TestSubscriber<DiscoveryParams> rotatedUpdateParams= new TestSubscriber<>();
    vm.outputs.updateParamsForPage().map(pair -> pair.first).subscribe(rotatedUpdateParams);
    final TestSubscriber<Integer> rotatedUpdatePage = new TestSubscriber<>();
    vm.outputs.updateParamsForPage().map(pair -> pair.second).subscribe(rotatedUpdatePage);

    vm.inputs.discoveryPagerAdapterCreatedPage(null, 0);
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 1);

    // Params and page should not update.
    rotatedUpdateParams.assertNoValues();
    rotatedUpdatePage.assertNoValues();
  }

  @Test
  public void testClearingPages() {
    final DiscoveryViewModel vm = new DiscoveryViewModel(environment());

    final TestSubscriber<List<Integer>> clearPages = new TestSubscriber<>();
    vm.outputs.clearPages().subscribe(clearPages);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    vm.intent(intent);

    // Notify activity when pager adapter pages are created.
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 0);
    vm.inputs.discoveryPagerAdapterCreatedPage(null, 1);

    clearPages.assertNoValues();

    vm.inputs.pageChanged(1);

    clearPages.assertNoValues();

    vm.inputs.pageChanged(4);

    clearPages.assertNoValues();

    // Select ART category from the drawer.
    vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    clearPages.assertValues(Arrays.asList(0, 1, 2, 3));

    vm.inputs.pageChanged(1);

    // Select MUSIC category from the drawer.
    vm.inputs.childFilterViewHolderRowClick(null,
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.musicCategory()).build())
        .build()
    );

    clearPages.assertValues(Arrays.asList(0, 1, 2, 3), Arrays.asList(0, 2, 3, 4));
  }
}
