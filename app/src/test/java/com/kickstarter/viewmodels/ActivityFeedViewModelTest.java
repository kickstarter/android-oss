package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ActivityFactory;
import com.kickstarter.mock.factories.SurveyResponseFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.viewmodels.ActivityFeedViewModel.ViewModel;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class ActivityFeedViewModelTest extends KSRobolectricTestCase {
  private ViewModel vm;
  private final TestSubscriber<List<Activity>> activityList = new TestSubscriber<>();
  private final TestSubscriber<Void> goToDiscovery = new TestSubscriber<>();
  private final TestSubscriber<Void> goToLogin = new TestSubscriber<>();
  private final TestSubscriber<Project> goToProject = new TestSubscriber<>();
  private final TestSubscriber<SurveyResponse> goToSurvey = new TestSubscriber<>();
  private final TestSubscriber<Boolean> loggedOutEmptyStateIsVisible = new TestSubscriber<>();
  private final TestSubscriber<Boolean> loggedInEmptyStateIsVisible = new TestSubscriber<>();
  private final TestSubscriber<Activity> startUpdateActivity = new TestSubscriber<>();
  private final TestSubscriber<List<SurveyResponse>> surveys = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ViewModel(environment);
    this.vm.outputs.activityList().subscribe(this.activityList);
    this.vm.outputs.goToDiscovery().subscribe(this.goToDiscovery);
    this.vm.outputs.goToLogin().subscribe(this.goToLogin);
    this.vm.outputs.goToProject().subscribe(this.goToProject);
    this.vm.outputs.goToSurvey().subscribe(this.goToSurvey);
    this.vm.outputs.loggedOutEmptyStateIsVisible().subscribe(this.loggedOutEmptyStateIsVisible);
    this.vm.outputs.loggedInEmptyStateIsVisible().subscribe(this.loggedInEmptyStateIsVisible);
    this.vm.outputs.startUpdateActivity().subscribe(this.startUpdateActivity);
    this.vm.outputs.surveys().subscribe(this.surveys);
  }

  @Test
  public void testActivitiesEmit() {
    setUpEnvironment(environment());

    // Swipe refresh.
    this.vm.inputs.refresh();

    // Activities should emit.
    this.activityList.assertValueCount(1);
    this.koalaTest.assertValue(KoalaEvent.ACTIVITY_VIEW);

    // Paginate.
    this.vm.inputs.nextPage();
    this.activityList.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.ACTIVITY_VIEW, KoalaEvent.ACTIVITY_LOAD_MORE);
  }

  @Test
  public void testClickingInterfaceElements() {
    this.setUpEnvironment(this.environment());

    this.goToDiscovery.assertNoValues();
    this.goToLogin.assertNoValues();
    this.goToProject.assertNoValues();
    this.startUpdateActivity.assertNoValues();
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

    this.startUpdateActivity.assertValueCount(1);
    this.koalaTest.assertValues(
      KoalaEvent.ACTIVITY_VIEW, KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.ACTIVITY_VIEW_ITEM,
      KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaEvent.VIEWED_UPDATE
    );
  }

  @Test
  public void testLoginFlow() {
    final ApiClientType apiClient = new MockApiClient();
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = this.environment().toBuilder()
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);

    // Empty activity feed with login button should be shown.
    this.loggedOutEmptyStateIsVisible.assertValue(true);

    // Login.
    this.vm.inputs.emptyActivityFeedLoginClicked(null);
    this.goToLogin.assertValueCount(1);
    currentUser.refresh(UserFactory.user());

    // Empty states are not shown when activities emit on successful login.
    this.activityList.assertValueCount(1);
    this.loggedOutEmptyStateIsVisible.assertValues(true, false);
    this.loggedInEmptyStateIsVisible.assertValue(false);
  }

  @Test
  public void testSurveys_LoggedOut() {
    final List<SurveyResponse> surveyResponses = Arrays.asList(
      SurveyResponseFactory.surveyResponse(),
      SurveyResponseFactory.surveyResponse()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<List<SurveyResponse>> fetchUnansweredSurveys() {
        return Observable.just(surveyResponses);
      }
    };

    final CurrentUserType currentUser = new MockCurrentUser();
    currentUser.logout();

    final Environment environment = this.environment().toBuilder()
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);
    this.vm.inputs.resume();

    this.surveys.assertNoValues();
  }

  @Test
  public void testStartUpdateActivity() {
    final Activity activity = ActivityFactory.updateActivity();
    setUpEnvironment(environment());

    this.vm.inputs.projectUpdateClicked(null, activity);
    this.startUpdateActivity.assertValues(activity);
  }

  @Test
  public void testSurveys_LoggedIn_SwipeRefreshed() {
    final CurrentUserType currentUser = new MockCurrentUser();
    currentUser.login(UserFactory.user(), "deadbeef");

    final Environment environment = this.environment().toBuilder()
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);
    this.surveys.assertValueCount(1);

    this.vm.inputs.refresh();
    this.surveys.assertValueCount(2);
  }
}
