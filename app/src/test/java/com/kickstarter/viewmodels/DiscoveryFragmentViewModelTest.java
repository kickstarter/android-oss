package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.models.OptimizelyFeature;
import com.kickstarter.libs.preferences.MockIntPreference;
import com.kickstarter.libs.utils.ExperimentData;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.mock.MockExperimentsClientType;
import com.kickstarter.mock.factories.ActivityEnvelopeFactory;
import com.kickstarter.mock.factories.ActivityFactory;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.data.Editorial;
import com.kickstarter.libs.utils.EventName;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class DiscoveryFragmentViewModelTest extends KSRobolectricTestCase {
  private DiscoveryFragmentViewModel.ViewModel vm;

  private final TestSubscriber<Activity> activityTest = new TestSubscriber<>();
  private final TestSubscriber<Boolean> hasProjects = new TestSubscriber<>();
  private final TestSubscriber<List<Pair<Project, DiscoveryParams>>> projects = new TestSubscriber<>();
  private final TestSubscriber<Editorial> shouldShowEditorial = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shouldShowEmptySavedView = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shouldShowOnboardingViewTest = new TestSubscriber<>();
  private final TestSubscriber<Boolean> showActivityFeed = new TestSubscriber<>();
  private final TestSubscriber<Boolean> showLoginTout = new TestSubscriber<>();
  private final TestSubscriber<Editorial> startEditorialActivity = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<>();
  private final TestSubscriber<Activity> startUpdateActivity = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new DiscoveryFragmentViewModel.ViewModel(environment);
    this.vm.outputs.activity().subscribe(this.activityTest);
    this.vm.outputs.projectList().map(ListUtils::nonEmpty).subscribe(this.hasProjects);
    this.vm.outputs.projectList().filter(ListUtils::nonEmpty).subscribe(this.projects);
    this.vm.outputs.shouldShowEditorial().subscribe(this.shouldShowEditorial);
    this.vm.outputs.shouldShowEmptySavedView().subscribe(this.shouldShowEmptySavedView);
    this.vm.outputs.shouldShowOnboardingView().subscribe(this.shouldShowOnboardingViewTest);
    this.vm.outputs.showActivityFeed().subscribe(this.showActivityFeed);
    this.vm.outputs.showLoginTout().subscribe(this.showLoginTout);
    this.vm.outputs.startEditorialActivity().subscribe(this.startEditorialActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.startUpdateActivity().subscribe(this.startUpdateActivity);
  }

  private void setUpInitialHomeAllProjectsParams() {
    this.vm.inputs.paramsFromActivity(DiscoveryParams.getDefaultParams(null).toBuilder().sort(DiscoveryParams.Sort.MAGIC).build());
    this.vm.inputs.rootCategories(CategoryFactory.rootCategories());
  }

  @Test
  public void testRefresh() {
    setUpEnvironment(environment());

    // Load initial params and root categories from activity.
    setUpInitialHomeAllProjectsParams();

    // Should emit current fragment's projects.
    this.hasProjects.assertValues(true);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed");

    //Page is refreshed
    this.vm.inputs.refresh();
    this.hasProjects.assertValues(true, true);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed");
  }

  @Test
  public void testProjectsEmitWithNewCategoryParams() {
    setUpEnvironment(environment());

    // Load initial params and root categories from activity.
    setUpInitialHomeAllProjectsParams();

    // Should emit current fragment's projects.
    this.hasProjects.assertValues(true);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed");

    // Select a new category.
    this.vm.inputs.paramsFromActivity(
      DiscoveryParams.builder()
        .category(CategoryFactory.artCategory())
        .sort(DiscoveryParams.Sort.MAGIC)
        .build()
    );

    // New projects load with new params.
    this.hasProjects.assertValues(true, true, true);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed");

    this.vm.inputs.clearPage();
    this.hasProjects.assertValues(true, true, true, false);
  }

  @Test
  public void testProjectsEmitWithNewSort() {
    setUpEnvironment(environment());

    // Initial load.
    setUpInitialHomeAllProjectsParams();

    this.projects.assertValueCount(1);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed");

    // Popular tab clicked.
    this.vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
    this.projects.assertValueCount(3);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed");
  }

  @Test
  public void testProjectsRefreshAfterLogin() {
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);
    
    // Initial load.
    setUpInitialHomeAllProjectsParams();

    this.hasProjects.assertValue(true);

    // Projects should emit.
    this.projects.assertValueCount(1);

    // Log in.
    logUserIn(currentUser);

    // Projects should emit again.
    this.projects.assertValueCount(4);
  }

  @Test
  public void testShouldShowEditorial_otherParams() {
    setUpEnvironment(environment());

    // Art projects params.
    this.vm.inputs.paramsFromActivity(
      DiscoveryParams.builder()
        .category(CategoryFactory.artCategory())
        .sort(DiscoveryParams.Sort.MAGIC)
        .build()
    );

    this.shouldShowEditorial.assertValue(null);
  }

  @Test
  public void testShouldShowEditorial_defaultParams() {
    setUpEnvironment(environment());

    // Initial home all projects params.
    setUpInitialHomeAllProjectsParams();

    this.shouldShowEditorial.assertValue(null);
  }


  @Test
  public void testShouldShowEditorial_featureEnabled(){
    final MockCurrentUser user = new MockCurrentUser();
    final MockExperimentsClientType mockExperimentsClientType = new MockExperimentsClientType() {
      @Override
      public boolean isFeatureEnabled(final @NotNull OptimizelyFeature.Key feature, final @NotNull ExperimentData experimentData) {
        return true;
      }
    };

    final Environment environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .build();

    setUpEnvironment(environment);

    setUpInitialHomeAllProjectsParams();

    this.shouldShowEditorial.assertValue(Editorial.LIGHTS_ON);
  }

  @Test
  public void testShouldShowEditorial_featureDisabled(){
    final MockCurrentUser user = new MockCurrentUser();
    final MockExperimentsClientType mockExperimentsClientType = new MockExperimentsClientType() {
      @Override
      public boolean isFeatureEnabled(final @NotNull OptimizelyFeature.Key feature, final @NotNull ExperimentData experimentData) {
        return false;
      }
    };

    final Environment environment = environment().toBuilder()
            .currentUser(user)
            .optimizely(mockExperimentsClientType)
            .build();

    setUpEnvironment(environment);

    setUpInitialHomeAllProjectsParams();

    this.shouldShowEditorial.assertValue(null);
  }

  @Test
  public void testShouldShowEditorial_whenOptimizelyInitializationDelay(){
    final Environment environment = environment().toBuilder()
      .currentUser(new MockCurrentUser())
      .optimizely(new MockExperimentsClientType() {
        int enabledCount;
        @Override
        public boolean isFeatureEnabled(final @NonNull OptimizelyFeature.Key feature, final @NonNull ExperimentData experimentData) {
          if (enabledCount == 0) {
            enabledCount += 1;
            return false;
          } else {
            return true;
          }
        }
      })
      .build();

    setUpEnvironment(environment);

    setUpInitialHomeAllProjectsParams();

    this.shouldShowEditorial.assertValue(null);

    this.vm.optimizelyReady();
    this.shouldShowEditorial.assertValues(null, Editorial.LIGHTS_ON);
  }

  @Test
  public void testShouldShowEmptySavedView_isFalse_whenUserHasSavedProjects() {
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .apiClient(new MockApiClient())
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);

    // Initial home all projects params.
    setUpInitialHomeAllProjectsParams();

    this.hasProjects.assertValue(true);
    this.shouldShowEmptySavedView.assertValue(false);

    // Login.
    logUserIn(currentUser);

    // New projects load.
    this.hasProjects.assertValues(true, true, true, true);
    this.shouldShowEmptySavedView.assertValues(false);

    // Saved projects params.
    this.vm.inputs.paramsFromActivity(DiscoveryParams.builder().starred(1).build());

    // New projects load with updated params.
    this.hasProjects.assertValues(true, true, true, true, true, true);
    this.shouldShowEmptySavedView.assertValues(false);
  }

  @Test
  public void testShouldShowEmptySavedView_isTrue_whenUserHasNoSavedProjects() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        if (params.isSavedProjects()) {
          return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(new ArrayList<>()));
        } else {
          return super.fetchProjects(params);
        }
      }
    };

    final Environment environment = environment().toBuilder()
      .apiClient(apiClient)
      .currentUser(currentUser)
      .build();

    setUpEnvironment(environment);

    // Initial home all projects params.
    setUpInitialHomeAllProjectsParams();

    this.hasProjects.assertValue(true);
    this.shouldShowEmptySavedView.assertValue(false);

    // Login.
    logUserIn(currentUser);

    // New projects load.
    this.hasProjects.assertValues(true, true, true, true);
    this.shouldShowEmptySavedView.assertValues(false);

    // Saved projects params.
    this.vm.inputs.paramsFromActivity(DiscoveryParams.builder().starred(1).build());

    // Projects are cleared, new projects load with updated params.
    this.hasProjects.assertValues(true, true, true, true, false, false);
    this.shouldShowEmptySavedView.assertValues(false, true);
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

    setUpEnvironment(environment);

    // Initial home all projects params.
    setUpInitialHomeAllProjectsParams();

    // Should show onboarding view.
    this.shouldShowOnboardingViewTest.assertValues(true);
    this.activityTest.assertNoValues();

    // Change params. Onboarding view should not be shown.
    this.vm.inputs.paramsFromActivity(DiscoveryParams.builder().sort(DiscoveryParams.Sort.NEWEST).build());
    this.shouldShowOnboardingViewTest.assertValues(true, false);
    this.activityTest.assertNoValues();

    // Login.
    logUserIn(currentUser);

    // Activity sampler should be shown rather than onboarding view.
    this.shouldShowOnboardingViewTest.assertValues(true, false, false, false);
    this.activityTest.assertValues(null, activity);

    // Change params. Activity sampler should not be shown.
    this.vm.inputs.paramsFromActivity(DiscoveryParams.builder().build());
    this.activityTest.assertValues(null, activity, null);
  }

  @Test
  public void testShowLoginTout() {
    setUpEnvironment(environment());

    // Clicking login on onboarding view should show login tout.
    this.vm.inputs.discoveryOnboardingViewHolderLoginToutClick(null);

    this.showLoginTout.assertValue(true);
    this.lakeTest.assertValue("Log In or Signup Button Clicked");
  }

  @Test
  public void testStartEditorialActivity() {
    setUpEnvironment(environment());

    // Load initial params and root categories from activity.
    setUpInitialHomeAllProjectsParams();

    // Click on editorial
    this.vm.inputs.editorialViewHolderClicked(Editorial.GO_REWARDLESS);
    this.vm.inputs.editorialViewHolderClicked(Editorial.LIGHTS_ON);

    this.startEditorialActivity.assertValues(Editorial.GO_REWARDLESS, Editorial.LIGHTS_ON);
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", "Editorial Card Clicked", "Editorial Card Clicked");
  }

  @Test
  public void testStartProjectActivity_whenViewingEditorial() {
    setUpEnvironment(environment());

    // Load editorial params and root categories from activity.
    final DiscoveryParams editorialParams = DiscoveryParams.builder()
      .tagId(Editorial.GO_REWARDLESS.getTagId())
      .sort(DiscoveryParams.Sort.MAGIC)
      .build();
    this.vm.inputs.paramsFromActivity(editorialParams);
    this.vm.inputs.rootCategories(CategoryFactory.rootCategories());

    // Click on project
    final Project project = ProjectFactory.project();
    this.vm.inputs.projectCardViewHolderClicked(project);

    this.startProjectActivity.assertValue(Pair.create(project, RefTag.collection(518)));
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", EventName.CTA_CLICKED.getEventName());
    this.segmentTrack.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", EventName.CTA_CLICKED.getEventName());
  }

  @Test
  public void testStartProjectActivity_whenViewingAllProjects() {
    setUpEnvironment(environment());

    // Load initial params and root categories from activity.
    setUpInitialHomeAllProjectsParams();

    // Click on project
    final Project project = ProjectFactory.project();
    this.vm.inputs.projectCardViewHolderClicked(project);

    this.startProjectActivity.assertValue(Pair.create(project, RefTag.discovery()));
    this.lakeTest.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", EventName.CTA_CLICKED.getEventName());
    this.segmentTrack.assertValues(EventName.PAGE_VIEWED.getEventName(), "Explore Page Viewed", EventName.CTA_CLICKED.getEventName());
  }

  @Test
  public void testClickingInterfaceElements() {
    setUpEnvironment(environment());

    // Clicking see activity feed button on sampler should show activity feed.
    this.showActivityFeed.assertNoValues();
    this.vm.inputs.activitySampleFriendBackingViewHolderSeeActivityClicked(null);
    this.showActivityFeed.assertValues(true);
    this.vm.inputs.activitySampleFriendFollowViewHolderSeeActivityClicked(null);
    this.showActivityFeed.assertValues(true, true);
    this.vm.inputs.activitySampleProjectViewHolderSeeActivityClicked(null);
    this.showActivityFeed.assertValues(true, true, true);

    // Clicking activity update on sampler should show activity update.
    this.startUpdateActivity.assertNoValues();
    this.vm.inputs.activitySampleProjectViewHolderUpdateClicked(null, ActivityFactory.updateActivity());
    this.startUpdateActivity.assertValueCount(1);
  }

  private void logUserIn(final @NonNull CurrentUserType currentUser) {
    final User user = UserFactory.user();
    currentUser.refresh(user);
    this.vm.inputs.paramsFromActivity(DiscoveryParams.getDefaultParams(user));
  }
}
