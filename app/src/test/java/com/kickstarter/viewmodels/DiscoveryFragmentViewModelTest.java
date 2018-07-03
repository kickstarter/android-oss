package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ActivityEnvelopeFactory;
import com.kickstarter.factories.ActivityFactory;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.preferences.MockIntPreference;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.ActivityEnvelope;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class DiscoveryFragmentViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProjectsEmitWithNewCategoryParams() {
    final DiscoveryFragmentViewModel.ViewModel vm = new DiscoveryFragmentViewModel.ViewModel(environment());

    final TestSubscriber<Boolean> hasProjects = new TestSubscriber<>();
    vm.outputs.projectList().map(ListUtils::nonEmpty).subscribe(hasProjects);

    // Load initial params and root categories from activity.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build());
    vm.inputs.rootCategories(CategoryFactory.rootCategories());

    // Should emit current fragment's projects.
    hasProjects.assertValues(true);
    koalaTest.assertValues("Discover List View");

    // Select a new category.
    vm.inputs.paramsFromActivity(
      DiscoveryParams.builder()
        .category(CategoryFactory.artCategory())
        .sort(DiscoveryParams.Sort.HOME)
        .build()
    );

    // Projects are cleared, new projects load.
    hasProjects.assertValues(true, false, true);
    koalaTest.assertValues("Discover List View", "Discover List View");

    vm.inputs.clearPage();
    hasProjects.assertValues(true, false, true, false);
  }

  @Test
  public void testProjectsEmitWithNewSort() {
    final DiscoveryFragmentViewModel.ViewModel vm = new DiscoveryFragmentViewModel.ViewModel(environment());

    final TestSubscriber<List<Project>> projects = new TestSubscriber<>();
    vm.outputs.projectList().filter(ListUtils::nonEmpty).subscribe(projects);

    // Initial load.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build());
    vm.inputs.rootCategories(CategoryFactory.rootCategories());

    projects.assertValueCount(1);
    koalaTest.assertValues("Discover List View");

    // Popular tab clicked.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
    projects.assertValueCount(2);
    koalaTest.assertValues("Discover List View", "Discover List View");
  }

  @Test
  public void testProjectsRefreshAfterLogin() {
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final DiscoveryFragmentViewModel.ViewModel vm = new DiscoveryFragmentViewModel.ViewModel(environment);

    final TestSubscriber<List<Project>> projects = new TestSubscriber<>();
    vm.outputs.projectList().filter(ListUtils::nonEmpty).subscribe(projects);

    // Initial load.
    vm.inputs.rootCategories(CategoryFactory.rootCategories());
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build());

    // Projects should emit.
    projects.assertValueCount(1);

    // Log in.
    currentUser.refresh(UserFactory.user());

    // Projects should emit again.
    projects.assertValueCount(2);
  }

  @Test
  public void testShowHeaderViews() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final Activity activity = ActivityFactory.activity();
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<ActivityEnvelope> fetchActivities() {
        return Observable.just(
          ActivityEnvelopeFactory.activityEnvelope(Collections.singletonList(activity))
        );
      }
    };
    final MockIntPreference activitySamplePreference = new MockIntPreference(987654321);

    final Environment environment = environment().toBuilder()
      .activitySamplePreference(activitySamplePreference)
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    final DiscoveryFragmentViewModel.ViewModel vm = new DiscoveryFragmentViewModel.ViewModel(environment);

    final TestSubscriber<Activity> activityTest = new TestSubscriber<>();
    vm.outputs.activity().subscribe(activityTest);

    final TestSubscriber<Boolean> shouldShowOnboardingViewTest = new TestSubscriber<>();
    vm.outputs.shouldShowOnboardingView().subscribe(shouldShowOnboardingViewTest);

    // Initial home all projects params.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).build());
    vm.inputs.rootCategories(CategoryFactory.rootCategories());

    // Should show onboarding view.
    shouldShowOnboardingViewTest.assertValues(true);
    activityTest.assertValue(null);

    // Change params. Onboarding view should not be shown.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());
    shouldShowOnboardingViewTest.assertValues(true, false);
    activityTest.assertValues(null, null);

    // Login.
    currentUser.refresh(UserFactory.user());

    // Activity sampler should be shown rather than onboarding view.
    shouldShowOnboardingViewTest.assertValues(true, false, false);
    activityTest.assertValues(null, null, activity);

    // Change params. Activity sampler should not be shown.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());
    activityTest.assertValues(null, null, activity, null);
  }

  @Test
  public void testClickingInterfaceElements() {
    final DiscoveryFragmentViewModel.ViewModel vm = new DiscoveryFragmentViewModel.ViewModel(environment());

    final TestSubscriber<Boolean> showActivityFeed = new TestSubscriber<>();
    vm.outputs.showActivityFeed().subscribe(showActivityFeed);

    final TestSubscriber<Activity> startUpdateActivity = new TestSubscriber<>();
    vm.outputs.startUpdateActivity().subscribe(startUpdateActivity);

    final TestSubscriber<Boolean> showLoginTout = new TestSubscriber<>();
    vm.outputs.showLoginTout().subscribe(showLoginTout);

    final TestSubscriber<Pair<Project, RefTag>> showProject = new TestSubscriber<>();
    vm.outputs.startProjectActivity().subscribe(showProject);

    // Clicking see activity feed button on sampler should show activity feed.
    showActivityFeed.assertNoValues();
    vm.inputs.activitySampleFriendBackingViewHolderSeeActivityClicked(null);
    showActivityFeed.assertValues(true);
    vm.inputs.activitySampleFriendFollowViewHolderSeeActivityClicked(null);
    showActivityFeed.assertValues(true, true);
    vm.inputs.activitySampleProjectViewHolderSeeActivityClicked(null);
    showActivityFeed.assertValues(true, true, true);

    // Clicking activity update on sampler should show activity update.
    startUpdateActivity.assertNoValues();
    vm.inputs.activitySampleProjectViewHolderUpdateClicked(null, ActivityFactory.updateActivity());
    startUpdateActivity.assertValueCount(1);
    koalaTest.assertValues(KoalaEvent.VIEWED_UPDATE);

    // Clicking login on onboarding view should show login tout.
    showLoginTout.assertNoValues();
    vm.inputs.discoveryOnboardingViewHolderLoginToutClick(null);
    showLoginTout.assertValue(true);

    // Pass in params and sort to fetch projects.
    vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());

    // Clicking on a project card should show project activity.
    showProject.assertNoValues();
    vm.inputs.projectCardViewHolderClicked(ProjectFactory.project());
    showProject.assertValueCount(1);
  }
}
