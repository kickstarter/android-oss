package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.mock.factories.ActivityFactory;
import com.kickstarter.mock.factories.SurveyResponseFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Activity;
import com.kickstarter.models.ErroredBacking;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.viewmodels.ActivityFeedViewModel.ViewModel;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class ActivityFeedViewModelTest extends KSRobolectricTestCase {
  private ViewModel vm;
  private final TestSubscriber<List<Activity>> activityList = new TestSubscriber<>();
  private final TestSubscriber<List<ErroredBacking>> erroredBackings = new TestSubscriber<>();
  private final TestSubscriber<Void> goToDiscovery = new TestSubscriber<>();
  private final TestSubscriber<Void> goToLogin = new TestSubscriber<>();
  private final TestSubscriber<Project> goToProject = new TestSubscriber<>();
  private final TestSubscriber<SurveyResponse> goToSurvey = new TestSubscriber<>();
  private final TestSubscriber<Boolean> loggedOutEmptyStateIsVisible = new TestSubscriber<>();
  private final TestSubscriber<Boolean> loggedInEmptyStateIsVisible = new TestSubscriber<>();
  private final TestSubscriber<String> startFixPledge = new TestSubscriber<>();
  private final TestSubscriber<Activity> startUpdateActivity = new TestSubscriber<>();
  private final TestSubscriber<List<SurveyResponse>> surveys = new TestSubscriber<>();
  private final TestSubscriber<User> user = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ViewModel(environment);
    this.vm.outputs.activityList().subscribe(this.activityList);
    this.vm.outputs.erroredBackings().subscribe(this.erroredBackings);
    this.vm.outputs.goToDiscovery().subscribe(this.goToDiscovery);
    this.vm.outputs.goToLogin().subscribe(this.goToLogin);
    this.vm.outputs.goToProject().subscribe(this.goToProject);
    this.vm.outputs.goToSurvey().subscribe(this.goToSurvey);
    this.vm.outputs.loggedOutEmptyStateIsVisible().subscribe(this.loggedOutEmptyStateIsVisible);
    this.vm.outputs.loggedInEmptyStateIsVisible().subscribe(this.loggedInEmptyStateIsVisible);
    this.vm.outputs.startFixPledge().subscribe(this.startFixPledge);
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

    // Paginate.
    this.vm.inputs.nextPage();
    this.activityList.assertValueCount(1);
    this.lakeTest.assertValue("Activity Feed Viewed");
  }

  @Test
  public void testClickingInterfaceElements() {
    this.setUpEnvironment(this.environment());

    this.goToDiscovery.assertNoValues();
    this.goToLogin.assertNoValues();
    this.goToProject.assertNoValues();
    this.startUpdateActivity.assertNoValues();

    // Empty activity feed clicks do not trigger events yet.
    this.vm.inputs.emptyActivityFeedDiscoverProjectsClicked(null);
    this.goToDiscovery.assertValueCount(1);

    this.vm.inputs.emptyActivityFeedLoginClicked(null);
    this.goToLogin.assertValueCount(1);

    this.vm.inputs.friendBackingClicked(null, ActivityFactory.friendBackingActivity());
    this.vm.inputs.projectStateChangedClicked(null, ActivityFactory.projectStateChangedActivity());
    this.vm.inputs.projectStateChangedPositiveClicked(null, ActivityFactory.projectStateChangedPositiveActivity());
    this.vm.inputs.projectUpdateProjectClicked(null, ActivityFactory.updateActivity());

    this.goToProject.assertValueCount(4);

    this.vm.inputs.projectUpdateClicked(null, ActivityFactory.activity());

    this.startUpdateActivity.assertValueCount(1);
    this.lakeTest.assertValue("Activity Feed Viewed");
  }

  @Test
  public void testErroredBackings_whenLoggedIn() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final User initialUser = UserFactory.user();
    currentUser.login(initialUser, "deadbeef");

    final User updatedUser = UserFactory.user();
    final Environment environment = this.environment().toBuilder()
      .apiClient(new MockApiClient(){
        @NonNull
        @Override
        public Observable<User> fetchCurrentUser() {
          return Observable.just(updatedUser);
        }
      })
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);
    this.erroredBackings.assertValueCount(1);

    this.vm.inputs.refresh();
    this.erroredBackings.assertValueCount(2);

    this.lakeTest.assertValue("Activity Feed Viewed");
  }

  @Test
  public void testErroredBackings_whenLoggedOut() {
    setUpEnvironment(environment());

    this.vm.inputs.resume();

    this.erroredBackings.assertNoValues();

    this.lakeTest.assertValue("Activity Feed Viewed");
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
  public void testStartFixPledge() {
    setUpEnvironment(environment());

    final String projectSlug = "slug";
    this.vm.inputs.managePledgeClicked(projectSlug);
    this.startFixPledge.assertValue(projectSlug);
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

  @Test
  public void testUser_LoggedIn_SwipeRefreshed() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final User initialUser = UserFactory.user().toBuilder().unseenActivityCount(3).build();
    currentUser.login(initialUser, "deadbeef");

    final User updatedUser = UserFactory.user();
    final Environment environment = this.environment().toBuilder()
      .apiClient(new MockApiClient(){
        @NonNull
        @Override
        public Observable<User> fetchCurrentUser() {
          return Observable.just(updatedUser);
        }
      })
      .currentUser(currentUser)
      .build();

    environment.currentUser().loggedInUser().subscribe(this.user);

    setUpEnvironment(environment);
    this.surveys.assertValueCount(1);
    this.user.assertValues(initialUser, updatedUser);

    this.vm.inputs.refresh();
    this.surveys.assertValueCount(2);
    this.user.assertValues(initialUser, updatedUser);
  }

  @Test
  public void testUser_whenLoggedInAndResumedWithErroredBackings() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final User initialUser = UserFactory.user()
      .toBuilder()
      .erroredBackingsCount(3)
      .build();
    currentUser.login(initialUser, "token");

    final User updatedUser = UserFactory.user();
    final Environment environment = this.environment().toBuilder()
      .apiClient(new MockApiClient() {
        @Override public @NonNull Observable<User> fetchCurrentUser() {
          return Observable.just(updatedUser);
        }
      })
      .currentUser(currentUser)
      .build();

    environment.currentUser().loggedInUser().subscribe(this.user);

    setUpEnvironment(environment);
    this.user.assertValues(initialUser, updatedUser);

    this.vm.inputs.resume();
    this.user.assertValues(initialUser, updatedUser);
  }
}
