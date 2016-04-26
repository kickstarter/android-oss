package com.kickstarter.viewmodels;

import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ActivityFactory;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.preferences.MockIntPreference;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.ArgumentsKey;

import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

public class DiscoveryFragmentViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProjectsEmitWithNewCategoryParams() {
    final DiscoveryFragmentViewModel vm = new DiscoveryFragmentViewModel(environment());

    final TestSubscriber<Boolean> hasProjects = new TestSubscriber<>();
    vm.outputs.projects().map(ps -> ps.size() > 0).subscribe(hasProjects);

    // Initialize the view model with MAGIC sort param.
    final Bundle bundle = new Bundle();
    bundle.putSerializable(ArgumentsKey.DISCOVERY_SORT_POSITION, 0);
    vm.arguments(bundle);

    // Load initial params from activity.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.MAGIC).build());

    // Should emit current fragment's projects.
    hasProjects.assertValues(false, true);
    koalaTest.assertValues("Discover List View");

    // Select a new category.
    vm.inputs.paramsFromActivity(
      DiscoveryParams.builder()
        .category(CategoryFactory.artCategory())
        .sort(DiscoveryParams.Sort.MAGIC)
        .build()
    );
    hasProjects.assertValues(false, true, false, true);
    koalaTest.assertValues("Discover List View", "Discover List View");

    vm.inputs.clearPage();
    hasProjects.assertValues(false, true, false, true, false);
  }

  @Test
  public void testProjectsEmitWithNewSort() {
    final DiscoveryFragmentViewModel vm = new DiscoveryFragmentViewModel(environment());

    final TestSubscriber<List<Project>> projects = new TestSubscriber<>();
    vm.outputs.projects().filter(ps -> ps.size() > 0).subscribe(projects);

    // Initialize the view model with MAGIC sort tab.
    final Bundle magicBundle = new Bundle();
    magicBundle.putSerializable(ArgumentsKey.DISCOVERY_SORT_POSITION, 0);
    vm.arguments(magicBundle);

    // Initial load.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.MAGIC).build());

    projects.assertValueCount(1);
    koalaTest.assertValues("Discover List View");

    // Popularity tab clicked.
    final Bundle popularBundle = new Bundle();
    popularBundle.putSerializable(ArgumentsKey.DISCOVERY_SORT_POSITION, 1);
    vm.arguments(popularBundle);

    vm.inputs.paramsFromActivity(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.POPULAR).build());
    projects.assertValueCount(2);
    koalaTest.assertValues("Discover List View", "Discover List View");
  }

  @Test
  public void testShowHeaderViews() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final ApiClientType apiClient = new MockApiClient();
    final MockIntPreference activitySamplePreference = new MockIntPreference(987654321);

    final Environment environment = environment().toBuilder()
      .activitySamplePreference(activitySamplePreference)
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    final DiscoveryFragmentViewModel vm = new DiscoveryFragmentViewModel(environment);

    final TestSubscriber<Activity> activity = new TestSubscriber<>();
    vm.outputs.activity().subscribe(activity);

    final TestSubscriber<Boolean> shouldShowOnboardingView = new TestSubscriber<>();
    vm.outputs.shouldShowOnboardingView().subscribe(shouldShowOnboardingView);

    // Initial magic staff pick params.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.MAGIC).build());

    // Should show onboarding view.
    shouldShowOnboardingView.assertValues(true);
    activity.assertValue(null);

    // Change params. Onboarding view should not be shown.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());
    shouldShowOnboardingView.assertValues(true, false);
    activity.assertValues(null, null);

    // Login.
    currentUser.refresh(UserFactory.user());

    // Activity sampler should be shown rather than onboarding view.
    shouldShowOnboardingView.assertValues(true, false, false);
    activity.assertValues(null, null, ActivityFactory.activity());

    // Change params. Activity sampler should not be shown.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());
    activity.assertValues(null, null, ActivityFactory.activity(), null);
  }

  @Test
  public void testClickingInterfaceElements() {
    final DiscoveryFragmentViewModel vm = new DiscoveryFragmentViewModel(environment());

    final TestSubscriber<Boolean> showActivityFeed = new TestSubscriber<>();
    vm.outputs.showActivityFeed().subscribe(showActivityFeed);

    final TestSubscriber<Activity> showActivityUpdate = new TestSubscriber<>();
    vm.outputs.showActivityUpdate().subscribe(showActivityUpdate);

    final TestSubscriber<Boolean> showLoginTout = new TestSubscriber<>();
    vm.outputs.showLoginTout().subscribe(showLoginTout);

    final TestSubscriber<Pair<Project, RefTag>> showProject = new TestSubscriber<>();
    vm.outputs.showProject().subscribe(showProject);

    // Start the view model with a MAGIC sort param.
    final Bundle bundle = new Bundle();
    bundle.putSerializable(ArgumentsKey.DISCOVERY_SORT_POSITION, 0);
    vm.arguments(bundle);

    // Clicking see activity feed button on sampler should show activity feed.
    showActivityFeed.assertNoValues();
    vm.inputs.activitySampleFriendBackingViewHolderSeeActivityClicked(null);
    showActivityFeed.assertValues(true);
    vm.inputs.activitySampleFriendFollowViewHolderSeeActivityClicked(null);
    showActivityFeed.assertValues(true, true);
    vm.inputs.activitySampleProjectViewHolderSeeActivityClicked(null);
    showActivityFeed.assertValues(true, true, true);

    // Clicking activity update on sampler should show activity update.
    showActivityUpdate.assertNoValues();
    vm.inputs.activitySampleProjectViewHolderUpdateClicked(null, ActivityFactory.updateActivity());
    showActivityUpdate.assertValueCount(1);

    // Clicking login on onboarding view should show login tout.
    showLoginTout.assertNoValues();
    vm.inputs.discoveryOnboardingViewHolderLoginToutClick(null);
    showLoginTout.assertValue(true);

    // Pass in params and sort to fetch projects.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());

    // Clicking on a project card should show project activity.
    showProject.assertNoValues();
    vm.inputs.projectCardViewHolderClick(null, ProjectFactory.project());
    showProject.assertValueCount(1);
  }
}
