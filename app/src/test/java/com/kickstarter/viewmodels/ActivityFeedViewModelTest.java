package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.factories.ActivityFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;

import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

public class ActivityFeedViewModelTest extends KSRobolectricTestCase {
  private String koalaActivityLoadMoreString;
  private String koalaActivityViewString;
  private String koalaActivityViewItemString;
  private String koalaViewedUpdateString;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    this.koalaActivityLoadMoreString = context().getString(R.string.Activity_Load_More);
    this.koalaActivityViewString = context().getString(R.string.Activity_View);
    this.koalaActivityViewItemString = context().getString(R.string.Activity_View_Item);
    this.koalaViewedUpdateString = context().getString(R.string.Viewed_Update);
  }

  @Test
  public void testActivitiesEmit() {
    final ActivityFeedViewModel.ViewModel vm = new ActivityFeedViewModel.ViewModel(environment());

    final TestSubscriber<List<Activity>> activities = new TestSubscriber<>();
    vm.outputs.activities().subscribe(activities);

    // Initialize the paginator.
    vm.inputs.refresh();

    // Activities should emit.
    activities.assertValueCount(1);
    koalaTest.assertValue(koalaActivityViewString);

    // Paginate.
    vm.inputs.nextPage();
    activities.assertValueCount(1);
    koalaTest.assertValues(koalaActivityViewString, koalaActivityLoadMoreString);
  }

  @Test
  public void testClickingInterfaceElements() {
    final ActivityFeedViewModel.ViewModel vm = new ActivityFeedViewModel.ViewModel(environment());

    final TestSubscriber<Void> goToDiscovery = new TestSubscriber<>();
    vm.outputs.goToDiscovery().subscribe(goToDiscovery);

    final TestSubscriber<Void> goToLogin = new TestSubscriber<>();
    vm.outputs.goToLogin().subscribe(goToLogin);

    final TestSubscriber<Project> goToProject = new TestSubscriber<>();
    vm.outputs.goToProject().subscribe(goToProject);

    final TestSubscriber<Activity> goToProjectUpdate = new TestSubscriber<>();
    vm.outputs.goToProjectUpdate().subscribe(goToProjectUpdate);

    goToDiscovery.assertNoValues();
    goToLogin.assertNoValues();
    goToProject.assertNoValues();
    goToProjectUpdate.assertNoValues();
    koalaTest.assertValues(koalaActivityViewString);

    // Empty activity feed clicks do not trigger events yet.
    vm.inputs.emptyActivityFeedDiscoverProjectsClicked(null);
    goToDiscovery.assertValueCount(1);

    vm.inputs.emptyActivityFeedLoginClicked(null);
    goToLogin.assertValueCount(1);

    vm.inputs.friendBackingClicked(null, ActivityFactory.friendBackingActivity());
    vm.inputs.projectStateChangedClicked(null, ActivityFactory.projectStateChangedActivity());
    vm.inputs.projectStateChangedPositiveClicked(null, ActivityFactory.projectStateChangedPositiveActivity());
    vm.inputs.projectUpdateProjectClicked(null, ActivityFactory.updateActivity());

    koalaTest.assertValues(
      koalaActivityViewString, koalaActivityViewItemString, koalaActivityViewItemString, koalaActivityViewItemString,
      koalaActivityViewItemString
    );
    goToProject.assertValueCount(4);

    vm.inputs.projectUpdateClicked(null, ActivityFactory.activity());

    goToProjectUpdate.assertValueCount(1);
    koalaTest.assertValues(
      koalaActivityViewString, koalaActivityViewItemString, koalaActivityViewItemString, koalaActivityViewItemString,
      koalaActivityViewItemString, koalaViewedUpdateString
    );
  }

  @Test
  public void testLoginFlow() {
    final ApiClientType apiClient = new MockApiClient();
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    final ActivityFeedViewModel.ViewModel vm = new ActivityFeedViewModel.ViewModel(environment);

    final TestSubscriber<List<Activity>> activities = new TestSubscriber<>();
    vm.outputs.activities().subscribe(activities);

    final TestSubscriber<Boolean> loggedOutEmptyStateIsVisible = new TestSubscriber<>();
    vm.outputs.loggedOutEmptyStateIsVisible().subscribe(loggedOutEmptyStateIsVisible);

    final TestSubscriber<Boolean> loggedInEmptyStateIsVisible = new TestSubscriber<>();
    vm.outputs.loggedInEmptyStateIsVisible().subscribe(loggedInEmptyStateIsVisible);

    final TestSubscriber<Void> goToLogin = new TestSubscriber<>();
    vm.outputs.goToLogin().subscribe(goToLogin);

    // Empty activity feed with login button should be shown.
    loggedOutEmptyStateIsVisible.assertValue(true);

    // Login.
    vm.inputs.emptyActivityFeedLoginClicked(null);
    goToLogin.assertValueCount(1);
    currentUser.refresh(UserFactory.user());

    // Empty states are not shown when activities emit on successful login.
    activities.assertValueCount(1);
    loggedOutEmptyStateIsVisible.assertValues(true, false);
    loggedInEmptyStateIsVisible.assertValue(false);
  }
}
