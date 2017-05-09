package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ActivityFactory;
import com.kickstarter.factories.SurveyResponseFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.viewmodels.ActivityFeedViewModel.ViewModel;

import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

public class ActivityFeedViewModelTest extends KSRobolectricTestCase {

  private ViewModel vm;

  final TestSubscriber<List<Activity>> activities = new TestSubscriber<>();
  final TestSubscriber<Void> goToDiscovery = new TestSubscriber<>();
  final TestSubscriber<Void> goToLogin = new TestSubscriber<>();
  final TestSubscriber<Project> goToProject = new TestSubscriber<>();
  final TestSubscriber<Activity> goToProjectUpdate = new TestSubscriber<>();
  final TestSubscriber<SurveyResponse> goToSurvey = new TestSubscriber<>();
  final TestSubscriber<Boolean> loggedOutEmptyStateIsVisible = new TestSubscriber<>();
  final TestSubscriber<Boolean> loggedInEmptyStateIsVisible = new TestSubscriber<>();

  private void setUpEnvironment(@NonNull final Environment environment) {
    vm = new ViewModel(environment);

    vm.outputs.activities().subscribe(this.activities);
    vm.outputs.goToDiscovery().subscribe(this.goToDiscovery);
    vm.outputs.goToLogin().subscribe(this.goToLogin);
    vm.outputs.goToProject().subscribe(this.goToProject);
    vm.outputs.goToProjectUpdate().subscribe(this.goToProjectUpdate);
    vm.outputs.goToSurvey().subscribe(goToSurvey);
    vm.outputs.loggedOutEmptyStateIsVisible().subscribe(loggedOutEmptyStateIsVisible);
    vm.outputs.loggedInEmptyStateIsVisible().subscribe(loggedInEmptyStateIsVisible);
  }

  @Test
  public void testActivitiesEmit() {
    setUpEnvironment(environment());

    // Initialize the paginator.
    vm.inputs.refresh();

    // Activities should emit.
    activities.assertValueCount(1);
    koalaTest.assertValue(KoalaEvent.ACTIVITY_VIEW);

    // Paginate.
    vm.inputs.nextPage();
    activities.assertValueCount(1);
    koalaTest.assertValues(KoalaEvent.ACTIVITY_VIEW, KoalaEvent.ACTIVITY_LOAD_MORE);
  }

  @Test
  public void testClickingInterfaceElements() {
    this.setUpEnvironment(this.environment());

    this.goToDiscovery.assertNoValues();
    this.goToLogin.assertNoValues();
    this.goToProject.assertNoValues();
    this.goToProjectUpdate.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.ACTIVITY_VIEW);

    // Empty activity feed clicks do not trigger events yet.
    this.vm.inputs.emptyActivityFeedDiscoverProjectsClicked(null);
    this.goToDiscovery.assertValueCount(1);

    this.vm.inputs.emptyActivityFeedLoginClicked(null);
    this.goToLogin.assertValueCount(1);

    this.vm.inputs.friendBackingClicked(null, ActivityFactory.friendBackingActivity());
    this.vm.inputs.projectStateChangedClicked(null, ActivityFactory.projectStateChangedActivity());
    this.vm.inputs.projectStateChangedPositiveClicked(null, ActivityFactory.projectStateChangedPositiveActivity());
    this.vm.inputs.projectUpdateProjectClicked(null, ActivityFactory.updateActivity());

    this.koalaTest.assertValues(
      KoalaEvent.ACTIVITY_VIEW, KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.ACTIVITY_VIEW_ITEM,
      KoalaEvent.ACTIVITY_VIEW_ITEM
    );
    this.goToProject.assertValueCount(4);

    this.vm.inputs.projectUpdateClicked(null, ActivityFactory.activity());

    this.goToProjectUpdate.assertValueCount(1);
    this.koalaTest.assertValues(
      KoalaEvent.ACTIVITY_VIEW, KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.ACTIVITY_VIEW_ITEM,
      KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.VIEWED_UPDATE
    );
  }

  @Test
  public void testLoginFlow() {
    ApiClientType apiClient = new MockApiClient();
    CurrentUserType currentUser = new MockCurrentUser();

    Environment environment = this.environment().toBuilder()
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);

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

  @Test public void testSurveyClick() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();

    setUpEnvironment(environment());

    vm.inputs.surveyClicked(null, surveyResponse);
    goToSurvey.assertValue(surveyResponse);
  }
}
