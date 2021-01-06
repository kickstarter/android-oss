package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.models.OptimizelyExperiment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.mock.MockExperimentsClientType;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.mock.factories.LocationFactory;
import com.kickstarter.mock.factories.ProjectDataFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApolloClient;
import com.kickstarter.models.Category;
import com.kickstarter.models.CreatorDetails;
import com.kickstarter.models.Location;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.data.ProjectData;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public final class ProjectHolderViewModelTest extends KSRobolectricTestCase {
  private ProjectHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> avatarPhotoUrl = new TestSubscriber<>();
  private final TestSubscriber<String> backersCountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backingViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> blurbTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> blurbVariantIsVisible = new TestSubscriber<>();
  private final TestSubscriber<String> categoryTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> commentsCountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> conversionPledgedAndGoalText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> conversionTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<Integer, Integer>> creatorBackedAndLaunchedProjectsCount = new TestSubscriber<>();
  private final TestSubscriber<Boolean> creatorDetailsLoadingContainerIsVisible = new TestSubscriber<>();
  private final TestSubscriber<Boolean> creatorDetailsVariantIsVisible = new TestSubscriber<>();
  private final TestSubscriber<String> creatorNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> deadlineCountdownTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> featuredTextViewRootCategory = new TestSubscriber<>();
  private final TestSubscriber<Boolean> featuredViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> goalStringForTextView = new TestSubscriber<>();
  private final TestSubscriber<String> locationTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Integer> percentageFundedProgress = new TestSubscriber<>();
  private final TestSubscriber<Boolean> percentageFundedProgressBarIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> playButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> pledgedTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Integer> projectDashboardButtonText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectDashboardContainerIsGone = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectDisclaimerGoalReachedDateTime = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, DateTime>> projectDisclaimerGoalNotReachedString = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectDisclaimerTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> projectLaunchDate = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectLaunchDateIsGone = new TestSubscriber<>();
  private final TestSubscriber<Integer> projectMetadataViewGroupBackgroundDrawableInt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectMetadataViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Project> projectOutput = new TestSubscriber<>();
  private final TestSubscriber<Photo> projectPhoto = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectSocialImageViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> projectSocialImageViewUrl = new TestSubscriber<>();
  private final TestSubscriber<List<User>> projectSocialTextViewFriends = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectSocialViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Integer> projectStateViewGroupBackgroundColorInt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectStateViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shouldSetDefaultStatsMargins = new TestSubscriber<>();
  private final TestSubscriber<Void> setCanceledProjectStateView = new TestSubscriber<>();
  private final TestSubscriber<Void> setProjectSocialClickListener = new TestSubscriber<>();
  private final TestSubscriber<DateTime> setSuccessfulProjectStateView = new TestSubscriber<>();
  private final TestSubscriber<Void> setSuspendedProjectStateView = new TestSubscriber<>();
  private final TestSubscriber<DateTime> setUnsuccessfulProjectStateView = new TestSubscriber<>();
  private final TestSubscriber<Project> startProjectSocialActivity = new TestSubscriber<>();
  private final TestSubscriber<String> updatesCountTextViewText = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment, final @NonNull ProjectData projectData) {
    this.vm = new ProjectHolderViewModel.ViewModel(environment);
    this.vm.outputs.avatarPhotoUrl().subscribe(this.avatarPhotoUrl);
    this.vm.outputs.backersCountTextViewText().subscribe(this.backersCountTextViewText);
    this.vm.outputs.backingViewGroupIsGone().subscribe(this.backingViewGroupIsGone);
    this.vm.outputs.blurbTextViewText().subscribe(this.blurbTextViewText);
    this.vm.outputs.blurbVariantIsVisible().subscribe(this.blurbVariantIsVisible);
    this.vm.outputs.categoryTextViewText().subscribe(this.categoryTextViewText);
    this.vm.outputs.commentsCountTextViewText().subscribe(this.commentsCountTextViewText);
    this.vm.outputs.conversionPledgedAndGoalText().subscribe(this.conversionPledgedAndGoalText);
    this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionTextViewIsGone);
    this.vm.outputs.creatorBackedAndLaunchedProjectsCount().subscribe(this.creatorBackedAndLaunchedProjectsCount);
    this.vm.outputs.creatorDetailsLoadingContainerIsVisible().subscribe(this.creatorDetailsLoadingContainerIsVisible);
    this.vm.outputs.creatorDetailsVariantIsVisible().subscribe(this.creatorDetailsVariantIsVisible);
    this.vm.outputs.creatorNameTextViewText().subscribe(this.creatorNameTextViewText);
    this.vm.outputs.deadlineCountdownTextViewText().subscribe(this.deadlineCountdownTextViewText);
    this.vm.outputs.featuredTextViewRootCategory().subscribe(this.featuredTextViewRootCategory);
    this.vm.outputs.featuredViewGroupIsGone().subscribe(this.featuredViewGroupIsGone);
    this.vm.outputs.goalStringForTextView().subscribe(this.goalStringForTextView);
    this.vm.outputs.locationTextViewText().subscribe(this.locationTextViewText);
    this.vm.outputs.percentageFundedProgress().subscribe(this.percentageFundedProgress);
    this.vm.outputs.percentageFundedProgressBarIsGone().subscribe(this.percentageFundedProgressBarIsGone);
    this.vm.outputs.playButtonIsGone().subscribe(this.playButtonIsGone);
    this.vm.outputs.pledgedTextViewText().subscribe(this.pledgedTextViewText);
    this.vm.outputs.projectDashboardButtonText().subscribe(this.projectDashboardButtonText);
    this.vm.outputs.projectDashboardContainerIsGone().subscribe(this.projectDashboardContainerIsGone);
    this.vm.outputs.projectDisclaimerGoalReachedDateTime().subscribe(this.projectDisclaimerGoalReachedDateTime);
    this.vm.outputs.projectDisclaimerGoalNotReachedString().subscribe(this.projectDisclaimerGoalNotReachedString);
    this.vm.outputs.projectDisclaimerTextViewIsGone().subscribe(this.projectDisclaimerTextViewIsGone);
    this.vm.outputs.projectLaunchDate().subscribe(this.projectLaunchDate);
    this.vm.outputs.projectLaunchDateIsGone().subscribe(this.projectLaunchDateIsGone);
    this.vm.outputs.projectMetadataViewGroupBackgroundDrawableInt().subscribe(this.projectMetadataViewGroupBackgroundDrawableInt);
    this.vm.outputs.projectMetadataViewGroupIsGone().subscribe(this.projectMetadataViewGroupIsGone);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.projectOutput().subscribe(this.projectOutput);
    this.vm.outputs.projectPhoto().subscribe(this.projectPhoto);
    this.vm.outputs.projectSocialImageViewIsGone().subscribe(this.projectSocialImageViewIsGone);
    this.vm.outputs.projectSocialImageViewUrl().subscribe(this.projectSocialImageViewUrl);
    this.vm.outputs.projectSocialTextViewFriends().subscribe(this.projectSocialTextViewFriends);
    this.vm.outputs.projectSocialViewGroupIsGone().subscribe(this.projectSocialViewGroupIsGone);
    this.vm.outputs.projectStateViewGroupBackgroundColorInt().subscribe(this.projectStateViewGroupBackgroundColorInt);
    this.vm.outputs.projectStateViewGroupIsGone().subscribe(this.projectStateViewGroupIsGone);
    this.vm.outputs.shouldSetDefaultStatsMargins().subscribe(this.shouldSetDefaultStatsMargins);
    this.vm.outputs.setCanceledProjectStateView().subscribe(this.setCanceledProjectStateView);
    this.vm.outputs.setProjectSocialClickListener().subscribe(this.setProjectSocialClickListener);
    this.vm.outputs.setSuccessfulProjectStateView().subscribe(this.setSuccessfulProjectStateView);
    this.vm.outputs.setSuspendedProjectStateView().subscribe(this.setSuspendedProjectStateView);
    this.vm.outputs.setUnsuccessfulProjectStateView().subscribe(this.setUnsuccessfulProjectStateView);
    this.vm.outputs.startProjectSocialActivity().subscribe(this.startProjectSocialActivity);
    this.vm.outputs.updatesCountTextViewText().subscribe(this.updatesCountTextViewText);

    this.vm.inputs.configureWith(projectData);
  }

  @Test
  public void testBlurbVariantIsVisible_whenControl() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.blurbVariantIsVisible.assertValue(false);
  }

  @Test
  public void testBlurbVariantIsVisible_whenVariant1() {
    setUpEnvironment(environmentForVariant(OptimizelyExperiment.Variant.VARIANT_1),
      ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.blurbVariantIsVisible.assertValue(true);
  }

  @Test
  public void testBlurbVariantIsVisible_whenVariant2() {
    setUpEnvironment(environmentForVariant(OptimizelyExperiment.Variant.VARIANT_2),
      ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.blurbVariantIsVisible.assertValue(true);
  }

  @Test
  public void testCreatorDataEmits() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.avatarPhotoUrl.assertValues(project.creator().avatar().medium());
    this.creatorNameTextViewText.assertValues(project.creator().name());
  }

  @Test
  public void testCreatorBackedAndLaunchedProjectsCount_whenFetchCreatorDetailsQuerySuccessful() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorBackedAndLaunchedProjectsCount.assertValue(Pair.create(3, 2));
  }

  @Test
  public void testCreatorBackedAndLaunchedProjectsCount_whenCreatorDetailsQueryUnsuccessful() {
    setUpEnvironment(environmentWithUnsuccessfulCreatorDetailsQuery(),
      ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorBackedAndLaunchedProjectsCount.assertNoValues();
  }

  @Test
  public void testCreatorDetailsVariantIsVisible_whenCreatorDetailsQueryUnsuccessful() {
    setUpEnvironment(environmentWithUnsuccessfulCreatorDetailsQuery(),
      ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorDetailsVariantIsVisible.assertValue(false);
  }

  @Test
  public void testCreatorDetailsVariantIsVisible_whenControl() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorDetailsVariantIsVisible.assertValue(false);
  }

  @Test
  public void testCreatorDetailsVariantIsVisible_whenVariant1() {
    setUpEnvironment(environmentForVariant(OptimizelyExperiment.Variant.VARIANT_1),
      ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorDetailsVariantIsVisible.assertValue(true);
  }

  @Test
  public void testCreatorDetailsLoadingContainerIsVisible_whenFetchCreatorDetailsQuerySuccessful() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorDetailsLoadingContainerIsVisible.assertValues(true, false);
  }

  @Test
  public void testCreatorDetailsLoadingContainerIsVisible_whenFetchCreatorDetailsQueryUnsuccessful() {
    setUpEnvironment(environmentWithUnsuccessfulCreatorDetailsQuery(),
      ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.creatorDetailsLoadingContainerIsVisible.assertValues(true, false);
  }

  @Test
  public void testMetadata_Backing() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.backedProject()));

    this.backingViewGroupIsGone.assertValues(false);
    this.featuredViewGroupIsGone.assertValues(true);
    this.projectMetadataViewGroupBackgroundDrawableInt.assertValues(R.drawable.rect_green_grey_stroke);
  }

  @Test
  public void testMetadata_Backing_Featured() {
    final Project project = ProjectFactory.featured()
      .toBuilder()
      .isBacking(true)
      .featuredAt(DateTime.now())
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.backingViewGroupIsGone.assertValues(false);
    this.featuredTextViewRootCategory.assertNoValues();
    this.featuredViewGroupIsGone.assertValues(true);
    this.projectMetadataViewGroupBackgroundDrawableInt.assertValues(R.drawable.rect_green_grey_stroke);
  }

  @Test
  public void testMetadata_Featured() {
    final Category category = CategoryFactory.textilesCategory();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(category)
      .featuredAt(DateTime.now())
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.backingViewGroupIsGone.assertValues(true);
    this.featuredTextViewRootCategory.assertValues(category.root().name());
    this.featuredViewGroupIsGone.assertValues(false);
    this.projectMetadataViewGroupBackgroundDrawableInt.assertNoValues();
  }

  @Test
  public void testMetadata_NoMetadata() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.backingViewGroupIsGone.assertValues(true);
    this.featuredTextViewRootCategory.assertNoValues();
    this.featuredViewGroupIsGone.assertValues(true);
    this.projectMetadataViewGroupBackgroundDrawableInt.assertNoValues();
    this.projectMetadataViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testPlayButton_Gone() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .video(null)
      .build();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.playButtonIsGone.assertValues(true);
  }

  @Test
  public void testPlayButton_Visible() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.playButtonIsGone.assertValues(false);
  }

  @Test
  public void testProgressBar_Visible() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_LIVE)
      .build();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.percentageFundedProgress.assertValues(ProgressBarUtils.progress(project.percentageFunded()));
    this.percentageFundedProgressBarIsGone.assertValues(false);
  }

  @Test
  public void testProgressBar_Gone() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.successfulProject()));

    this.percentageFundedProgressBarIsGone.assertValues(true);
  }

  @Test
  public void testProjectDashboardButtonText_whenCurrentUserIsNotProjectCreator() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.projectDashboardButtonText.assertNoValues();
  }

  @Test
  public void testProjectDashboardButtonText_whenCurrentUserIsProjectCreator_projectIsLive() {
    final User creator = UserFactory.creator();
    final Project project = ProjectFactory.project()
      .toBuilder()
      .creator(creator)
      .build();
    final Environment environment = environment()
      .toBuilder()
      .currentUser(new MockCurrentUser(creator))
      .build();
    setUpEnvironment(environment, ProjectDataFactory.Companion.project(project));

    this.projectDashboardButtonText.assertValue(R.string.View_progress);
  }

  @Test
  public void testProjectDashboardButtonText_whenCurrentUserIsProjectCreator_projectIsNotLive() {
    final User creator = UserFactory.creator();
    final Project project = ProjectFactory.successfulProject()
      .toBuilder()
      .creator(creator)
      .build();
    final Environment environment = environment()
      .toBuilder()
      .currentUser(new MockCurrentUser(creator))
      .build();
    setUpEnvironment(environment, ProjectDataFactory.Companion.project(project));

    this.projectDashboardButtonText.assertValue(R.string.View_dashboard);
  }

  @Test
  public void testProjectDashboardContainerIsGone_whenCurrentUserIsNotProjectCreator() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.projectDashboardContainerIsGone.assertValue(true);
  }

  @Test
  public void testProjectDashboardContainerIsGone_whenCurrentUserIsProjectCreator() {
    final User creator = UserFactory.creator();
    final Project project = ProjectFactory.successfulProject()
      .toBuilder()
      .creator(creator)
      .build();
    final Environment environment = environment()
      .toBuilder()
      .currentUser(new MockCurrentUser(creator))
      .build();
    setUpEnvironment(environment, ProjectDataFactory.Companion.project(project));

    this.projectDashboardContainerIsGone.assertValue(false);
  }

  @Test
  public void testProjectDataEmits() {
    final Category category = CategoryFactory.tabletopGamesCategory();
    final Location location = LocationFactory.unitedStates();
    final Project project = ProjectFactory.project()
      .toBuilder()
      .commentsCount(5000)
      .category(category)
      .location(location)
      .updatesCount(10)
      .build();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.blurbTextViewText.assertValues(project.blurb());
    this.categoryTextViewText.assertValues(category.name());
    this.commentsCountTextViewText.assertValues("5,000");
    this.goalStringForTextView.assertValueCount(1);
    this.locationTextViewText.assertValues(location.displayableName());
    this.pledgedTextViewText.assertValueCount(1);
    this.projectNameTextViewText.assertValues(project.name());
    this.projectOutput.assertValues(project);
    this.projectPhoto.assertValues(project.photo());
    this.updatesCountTextViewText.assertValues("10");
  }

  @Test
  public void testProjectDisclaimer_GoalReached() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_LIVE)
      .goal(100f)
      .pledged(500f)
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectDisclaimerGoalReachedDateTime.assertValueCount(1);
    this.projectDisclaimerTextViewIsGone.assertValues(false);
  }

  @Test
  public void testProjectDisclaimer_GoalNotReached() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .deadline(DateTime.now())
      .state(Project.STATE_LIVE)
      .goal(100f)
      .pledged(50f)
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectDisclaimerGoalNotReachedString.assertValueCount(1);
    this.projectDisclaimerTextViewIsGone.assertValues(false);
  }

  @Test
  public void testProjectDisclaimer_NoDisclaimer() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.successfulProject()));

    // Disclaimer is not shown for completed projects.
    this.projectDisclaimerTextViewIsGone.assertValues(true);
  }

  @Test
  public void testProjectLaunchDate_whenLaunchedAtIsNull() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .launchedAt(null)
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectLaunchDate.assertNoValues();
  }

  @Test
  public void testProjectLaunchDate_whenLaunchedAtIsNotNull() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .launchedAt(DateTime.parse("2019-11-05T14:21:42Z"))
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectLaunchDate.assertValue("November 5, 2019");
  }

  @Test
  public void testProjectLaunchDateIsGone_whenCurrentUserIsProjectCreator() {
    final User creator = UserFactory.creator();
    final Project project = ProjectFactory.project()
      .toBuilder()
      .creator(creator)
      .build();
    final Environment environment = environment()
      .toBuilder()
      .currentUser(new MockCurrentUser(creator))
      .build();

    setUpEnvironment(environment, ProjectDataFactory.Companion.project(project));

    this.projectLaunchDateIsGone.assertValue(false);
  }

  @Test
  public void testProjectLaunchDateIsGone_whenCurrentUserIsNotProjectCreator() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.projectLaunchDateIsGone.assertValue(true);
  }

  @Test
  public void testProjectLaunchDateIsGone_whenLaunchedAtIsNull() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .launchedAt(null)
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectLaunchDateIsGone.assertValue(true);
  }

  @Test
  public void testProjectSocialView_Clickable() {
    final User myFriend = UserFactory.germanUser();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Arrays.asList(myFriend, myFriend, myFriend))
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    // On click listener should be set for view with > 2 friends.
    this.setProjectSocialClickListener.assertValueCount(1);
    this.projectSocialImageViewIsGone.assertValues(false);
    this.projectSocialImageViewUrl.assertValueCount(1);
    this.projectSocialTextViewFriends.assertValueCount(1);
    this.projectSocialViewGroupIsGone.assertValues(false);
    this.shouldSetDefaultStatsMargins.assertValues(false);

    this.vm.inputs.projectSocialViewGroupClicked();
    this.startProjectSocialActivity.assertValues(project);
  }

  @Test
  public void testProjectSocialView_NoSocial_LoggedIn() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.emptyList())
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectSocialImageViewIsGone.assertValues(true);
    this.projectSocialImageViewUrl.assertNoValues();
    this.projectSocialTextViewFriends.assertNoValues();
    this.projectSocialViewGroupIsGone.assertValues(true);
    this.shouldSetDefaultStatsMargins.assertValues(true);
    this.setProjectSocialClickListener.assertNoValues();
  }

  @Test
  public void testProjectSocialView_NoSocial_LoggedOut() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(null)
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectSocialImageViewIsGone.assertValues(true);
    this.projectSocialImageViewUrl.assertNoValues();
    this.projectSocialTextViewFriends.assertNoValues();
    this.projectSocialViewGroupIsGone.assertValues(true);
    this.shouldSetDefaultStatsMargins.assertValues(true);
    this.setProjectSocialClickListener.assertNoValues();
  }

  @Test
  public void testProjectSocialView_NotClickable() {
    final User myFriend = UserFactory.germanUser();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(myFriend))
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    // On click listener should be not set for view with < 2 friends.
    this.setProjectSocialClickListener.assertNoValues();
    this.projectSocialImageViewIsGone.assertValues(false);
    this.projectSocialImageViewUrl.assertValueCount(1);
    this.projectSocialTextViewFriends.assertValueCount(1);
    this.projectSocialViewGroupIsGone.assertValues(false);
    this.shouldSetDefaultStatsMargins.assertValues(false);
  }

  @Test
  public void testProjectState_Canceled() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_CANCELED)
      .build();

    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectStateViewGroupBackgroundColorInt.assertValues(R.color.kds_support_300);
    this.projectStateViewGroupIsGone.assertValues(false);
    this.setCanceledProjectStateView.assertValueCount(1);
  }

  @Test
  public void testProjectState_Live() {
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(ProjectFactory.project()));

    this.projectStateViewGroupBackgroundColorInt.assertNoValues();
    this.projectStateViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testProjectState_Successful() {
    final DateTime stateChangedAt = DateTime.now();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .stateChangedAt(stateChangedAt)
      .build();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectStateViewGroupBackgroundColorInt.assertValues(R.color.green_alpha_50);
    this.projectStateViewGroupIsGone.assertValues(false);
    this.setSuccessfulProjectStateView.assertValues(stateChangedAt);
  }

  @Test
  public void testProjectState_Suspended() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_SUSPENDED)
      .build();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectStateViewGroupBackgroundColorInt.assertValues(R.color.kds_support_300);
    this.projectStateViewGroupIsGone.assertValues(false);
    this.setSuspendedProjectStateView.assertValueCount(1);
  }

  @Test
  public void testProjectState_Unsuccessful() {
    final DateTime stateChangedAt = DateTime.now();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_FAILED)
      .stateChangedAt(stateChangedAt)
      .build();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.projectStateViewGroupBackgroundColorInt.assertValues(R.color.kds_support_300);
    this.projectStateViewGroupIsGone.assertValues(false);
    this.setUnsuccessfulProjectStateView.assertValues(stateChangedAt);
  }

  @Test
  public void testProjectStatsEmit() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    this.backersCountTextViewText.assertValues(NumberUtils.format(project.backersCount()));
    this.deadlineCountdownTextViewText.assertValues(NumberUtils.format(ProjectUtils.deadlineCountdownValue(project)));
  }

  @Test
  public void testUsdConversionForNonUSProject() {
    // Use a CA project with a MX$ currency
    final Project project = ProjectFactory.mxCurrencyCAProject();
    final Config config = ConfigFactory.configForUSUser();
    final MockCurrentConfig currentConfig = new MockCurrentConfig();
    currentConfig.config(config);

    // Set the current config for a US user. KSCurrency needs this config for conversions.
    setUpEnvironment(environment().toBuilder().ksCurrency(new KSCurrency(currentConfig)).build(),
      ProjectDataFactory.Companion.project(project));

    // USD conversion shown for non US project.
    this.conversionPledgedAndGoalText.assertValueCount(1);
    this.conversionTextViewIsGone.assertValue(false);
  }

  @Test
  public void testUsdConversionNotShownForUSProject() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .country("US")
      .build();
    final Config config = ConfigFactory.configForUSUser();
    final MockCurrentConfig currentConfig = new MockCurrentConfig();
    currentConfig.config(config);
    setUpEnvironment(environment(), ProjectDataFactory.Companion.project(project));

    // USD conversion not shown for US project.
    this.conversionTextViewIsGone.assertValue(true);
  }

  private Environment environmentForVariant(final @NonNull OptimizelyExperiment.Variant variant) {
    return environment()
      .toBuilder()
      .optimizely(new MockExperimentsClientType(variant))
      .build();
  }

  private Environment environmentWithUnsuccessfulCreatorDetailsQuery() {
    return environment()
      .toBuilder()
      .apolloClient(new MockApolloClient() {
        @Override
        public @NotNull Observable<CreatorDetails> creatorDetails(final @NotNull String slug) {
          return Observable.error(new Throwable("failure"));
        }
      })
      .build();
  }
}
