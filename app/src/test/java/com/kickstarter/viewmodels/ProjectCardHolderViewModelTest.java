package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;


public class ProjectCardHolderViewModelTest extends KSRobolectricTestCase {
  private ProjectCardHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> backersCountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backingViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> deadlineCountdownText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> featuredViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> friendAvatar2IsHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> friendAvatar3IsHidden = new TestSubscriber<>();
  private final TestSubscriber<String> friendAvatarUrl1 = new TestSubscriber<>();
  private final TestSubscriber<String> friendAvatarUrl2= new TestSubscriber<>();
  private final TestSubscriber<String> friendAvatarUrl3 = new TestSubscriber<>();
  private final TestSubscriber<Boolean> friendBackingViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<List<User>> friendsForNamepile = new TestSubscriber<>();
  private final TestSubscriber<Boolean> fundingUnsuccessfulViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> fundingSuccessfulViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> imageIsInvisible = new TestSubscriber<>();
  private final TestSubscriber<Boolean> locationContainerIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> locationName = new TestSubscriber<>();
  private final TestSubscriber<Integer> metadataViewGroupBackgroundDrawable = new TestSubscriber<>();
  private final TestSubscriber<Boolean> metadataViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> nameAndBlurbText = new TestSubscriber<>();
  private final TestSubscriber<Project> notifyDelegateOfProjectClick = new TestSubscriber<>();
  private final TestSubscriber<Integer> percentageFundedForProgressBar = new TestSubscriber<>();
  private final TestSubscriber<String> percentageFundedTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> photoUrl = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectCanceledAt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectCardStatsViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectFailedAt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectStateViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectSubcategoryIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> projectSubcategoryName = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectSuccessfulAt = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectSuspendedAt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectTagContainerIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectWeLoveIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> rootCategoryNameForFeatured = new TestSubscriber<>();
  private final TestSubscriber<Boolean> setDefaultTopPadding = new TestSubscriber<>();
  private final TestSubscriber<Boolean> savedViewGroupIsGone = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProjectCardHolderViewModel.ViewModel(environment);
    this.vm.outputs.backersCountTextViewText().subscribe(this.backersCountTextViewText);
    this.vm.outputs.backingViewGroupIsGone().subscribe(this.backingViewGroupIsGone);
    this.vm.outputs.deadlineCountdownText().subscribe(this.deadlineCountdownText);
    this.vm.outputs.featuredViewGroupIsGone().subscribe(this.featuredViewGroupIsGone);
    this.vm.outputs.friendBackingViewIsHidden().subscribe(this.friendBackingViewIsHidden);
    this.vm.outputs.friendAvatar2IsGone().subscribe(this.friendAvatar2IsHidden);
    this.vm.outputs.friendAvatar3IsGone().subscribe(this.friendAvatar3IsHidden);
    this.vm.outputs.friendAvatarUrl1().subscribe(this.friendAvatarUrl1);
    this.vm.outputs.friendAvatarUrl2().subscribe(this.friendAvatarUrl2);
    this.vm.outputs.friendAvatarUrl3().subscribe(this.friendAvatarUrl3);
    this.vm.outputs.friendsForNamepile().subscribe(this.friendsForNamepile);
    this.vm.outputs.fundingUnsuccessfulViewGroupIsGone().subscribe(this.fundingUnsuccessfulViewGroupIsGone);
    this.vm.outputs.fundingSuccessfulViewGroupIsGone().subscribe(this.fundingSuccessfulViewGroupIsGone);
    this.vm.outputs.imageIsInvisible().subscribe(this.imageIsInvisible);
    this.vm.outputs.locationContainerIsGone().subscribe(this.locationContainerIsGone);
    this.vm.outputs.locationName().subscribe(this.locationName);
    this.vm.outputs.metadataViewGroupBackgroundDrawable().subscribe(this.metadataViewGroupBackgroundDrawable);
    this.vm.outputs.metadataViewGroupIsGone().subscribe(this.metadataViewGroupIsGone);
    this.vm.outputs.nameAndBlurbText().subscribe(this.nameAndBlurbText);
    this.vm.outputs.notifyDelegateOfProjectClick().subscribe(this.notifyDelegateOfProjectClick);
    this.vm.outputs.percentageFundedForProgressBar().subscribe(this.percentageFundedForProgressBar);
    this.vm.outputs.percentageFundedTextViewText().subscribe(this.percentageFundedTextViewText);
    this.vm.outputs.photoUrl().subscribe(this.photoUrl);
    this.vm.outputs.projectCanceledAt().subscribe(this.projectCanceledAt);
    this.vm.outputs.projectCardStatsViewGroupIsGone().subscribe(this.projectCardStatsViewGroupIsGone);
    this.vm.outputs.projectFailedAt().subscribe(this.projectFailedAt);
    this.vm.outputs.projectStateViewGroupIsGone().subscribe(this.projectStateViewGroupIsGone);
    this.vm.outputs.projectSubcategoryIsGone().subscribe(this.projectSubcategoryIsGone);
    this.vm.outputs.projectSubcategoryName().subscribe(this.projectSubcategoryName);
    this.vm.outputs.projectSuccessfulAt().subscribe(this.projectSuccessfulAt);
    this.vm.outputs.projectSuspendedAt().subscribe(this.projectSuspendedAt);
    this.vm.outputs.projectTagContainerIsGone().subscribe(this.projectTagContainerIsGone);
    this.vm.outputs.projectWeLoveIsGone().subscribe(this.projectWeLoveIsGone);
    this.vm.outputs.rootCategoryNameForFeatured().subscribe(this.rootCategoryNameForFeatured);
    this.vm.outputs.setDefaultTopPadding().subscribe(this.setDefaultTopPadding);
    this.vm.outputs.savedViewGroupIsGone().subscribe(this.savedViewGroupIsGone);
  }

  @Test
  public void testEmitsBackersCountTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(50).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.backersCountTextViewText.assertValues(NumberUtils.format(50));
  }

  @Test
  public void testBackingViewGroupIsGone_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.backingViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testBackingViewGroupIsGone_isStarred() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .isBacking(false)
      .isStarred(false)
      .featuredAt(null)
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.backingViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testEmitsDeadlineCountdownText() {
    final Project project = ProjectFactory.project().toBuilder()
      .deadline(new DateTime().plusSeconds(60 * 60 * 24 + 1))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.deadlineCountdownText.assertValues("24");
  }

  @Test
  public void testFeaturedViewGroupIsGone_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.featuredViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testFeaturedViewGroupIsGone_isFeatured() {
    final Project project = ProjectFactory.project().toBuilder().featuredAt(DateTime.now()).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.featuredViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testFriendAvatarUrl_withOneFriend() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.friendAvatarUrl1.assertValues(project.friends().get(0).avatar().small());
    this.friendAvatarUrl2.assertNoValues();
    this.friendAvatarUrl3.assertNoValues();
    this.friendAvatar2IsHidden.assertValue(true);
    this.friendAvatar3IsHidden.assertValue(true);
  }

  @Test
  public void testFriendAvatarUrl_withTwoFriends() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Arrays.asList(UserFactory.user(), UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.friendAvatarUrl1.assertValues(project.friends().get(0).avatar().small());
    this.friendAvatarUrl2.assertValues(project.friends().get(1).avatar().small());
    this.friendAvatarUrl3.assertNoValues();
    this.friendAvatar2IsHidden.assertValue(false);
    this.friendAvatar3IsHidden.assertValue(true);
  }

  @Test
  public void testFriendAvatarUrl_withThreeFriends() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Arrays.asList(UserFactory.user(), UserFactory.user(), UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.friendAvatarUrl1.assertValues(project.friends().get(0).avatar().small());
    this.friendAvatarUrl2.assertValues(project.friends().get(1).avatar().small());
    this.friendAvatarUrl3.assertValues(project.friends().get(2).avatar().small());
    this.friendAvatar2IsHidden.assertValue(false);
    this.friendAvatar3IsHidden.assertValue(false);
  }

  @Test
  public void testFriendBackingViewIsNotHidden() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));

    // friends view is not hidden for project with friend backings
    this.friendBackingViewIsHidden.assertValues(false);
  }

  @Test
  public void testEmitsFriendBackingViewIsHidden() {
    final Project project = ProjectFactory.project().toBuilder().friends(null).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.friendBackingViewIsHidden.assertValues(true);
  }

  @Test
  public void testFriendsForNamepile() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.friendsForNamepile.assertValues(project.friends());
  }

  @Test
  public void testFundingUnsuccessfulTextViewIsGone_projectLive() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.fundingUnsuccessfulViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testFundingUnsuccessfulViewGroupIsGone_projectFailed() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_FAILED).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.fundingUnsuccessfulViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testFundingSuccessfulViewGroupIsGone_projectFailed() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_FAILED).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.fundingSuccessfulViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testFundingSuccessfulViewGroupIsGone_projectSuccessful() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.fundingSuccessfulViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testEmitsImageIsInvisible() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.imageIsInvisible.assertValues(ObjectUtils.isNull(project.photo()));
  }

  @Test
  public void testLocationContainerIsGone_whenSortIsDistance() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    final DiscoveryParams discoveryParams = DiscoveryParams.builder()
      .sort(DiscoveryParams.Sort.DISTANCE)
      .build();
    this.vm.inputs.configureWith(Pair.create(project, discoveryParams));
    this.locationContainerIsGone.assertValues(false);
  }

  @Test
  public void testLocationContainerIsGone_whenSortIsNotDistance() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    final DiscoveryParams discoveryParams = DiscoveryParams.builder()
      .sort(DiscoveryParams.Sort.MAGIC)
      .build();
    this.vm.inputs.configureWith(Pair.create(project, discoveryParams));
    this.locationContainerIsGone.assertValues(true);
  }

  @Test
  public void testLocationName_whenLocationIsNull() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .location(null)
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.locationName.assertNoValues();
  }

  @Test
  public void testLocationName_whenLocationIsNotNull() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.locationName.assertValue("Brooklyn, NY");
  }

  @Test
  public void testMetadataViewGroupBackgroundColor() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.metadataViewGroupBackgroundDrawable.assertValues(R.drawable.rect_green_grey_stroke);
  }

  @Test
  public void testEmitsMetadataViewGroupIsGone() {
    final Project project = ProjectFactory.project().toBuilder().isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.metadataViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testEmitsNameAndBlurbText() {
    final Pair<String, String> nameAndBlurbPair = Pair.create("Farquaad", "Somebody once told me");
    final Project project = ProjectFactory.project().toBuilder().name(nameAndBlurbPair.first).blurb(nameAndBlurbPair.second).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.nameAndBlurbText.assertValues(nameAndBlurbPair);
  }

  @Test
  public void testNotifyDelegateOfProjectNameClick() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.vm.inputs.projectCardClicked();
    this.notifyDelegateOfProjectClick.assertValues(project);
  }

  @Test
  public void testPercentageFunded_projectSuccessful() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.percentageFundedForProgressBar.assertValues(ProgressBarUtils.progress(project.percentageFunded()));
  }

  @Test
  public void testPercentageFunded_projectFailed() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_FAILED).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.percentageFundedForProgressBar.assertValues(ProgressBarUtils.progress(0.0f));
  }

  @Test
  public void testPercentageFundedTextViewText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.percentageFundedTextViewText.assertValues(NumberUtils.flooredPercentage(project.percentageFunded()));
  }

  @Test
  public void testEmitsPhotoUrl() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.photoUrl.assertValues(project.photo().full());
  }

  @Test
  public void testProjectCanceledAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_CANCELED)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectCanceledAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testProjectCardStatsViewGroupIsGone_isLive() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectCardStatsViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testProjectCardStatsViewGroupIsGone_isCanceled() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_CANCELED).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectCardStatsViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testProjectFailedAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_FAILED)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectFailedAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testProjectStateViewGroupIsGone_projectLive() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectStateViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testProjectStateViewGroupIsGone_projectSuccessful() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectStateViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testProjectSubcategoryIsGone() {
    setUpEnvironment(environment());
    final Project artProject = ProjectFactory.project().toBuilder().category(CategoryFactory.artCategory()).build();
    final Project ceramicsProject = ProjectFactory.project().toBuilder().category(CategoryFactory.ceramicsCategory()).build();

    final DiscoveryParams allProjects = DiscoveryParams.builder().build();
    final DiscoveryParams artProjects = DiscoveryParams.builder().category(CategoryFactory.artCategory()).build();
    final DiscoveryParams ceramicsProjects = DiscoveryParams.builder().category(CategoryFactory.ceramicsCategory()).build();

    //Root category is shown for project without subcategory when viewing all projects.
    this.vm.inputs.configureWith(Pair.create(artProject, allProjects));
    this.projectSubcategoryIsGone.assertValue(false);

    //Subcategory is shown when viewing all projects.
    this.vm.inputs.configureWith(Pair.create(ceramicsProject, allProjects));
    this.projectSubcategoryIsGone.assertValue(false);

    this.vm.inputs.configureWith(Pair.create(ceramicsProject, artProjects));
    this.projectSubcategoryIsGone.assertValue(false);

    this.vm.inputs.configureWith(Pair.create(ceramicsProject, ceramicsProjects));
    this.projectSubcategoryIsGone.assertValues(false, true);

    this.vm.inputs.configureWith(Pair.create(ceramicsProject, artProjects));
    this.projectSubcategoryIsGone.assertValues(false, true, false);

    this.vm.inputs.configureWith(Pair.create(artProject, artProjects));
    this.projectSubcategoryIsGone.assertValues(false, true, false, true);
  }

  @Test
  public void testProjectSubcategoryName() {
    final Category category = CategoryFactory.ceramicsCategory();
    final Project project = ProjectFactory.project().toBuilder().category(category).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectSubcategoryName.assertValues(category.name());
  }

  @Test
  public void testProjectSuccessfulAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectSuccessfulAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testProjectSuspendedAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_SUSPENDED)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.projectSuspendedAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testProjectTagContainerIsGone() {
    setUpEnvironment(environment());

    final Project artProject = ProjectFactory.project().toBuilder().category(CategoryFactory.artCategory()).build();
    final Project ceramicsProject = ProjectFactory.project().toBuilder().category(CategoryFactory.ceramicsCategory()).build();
    final Project ceramicsStaffPickProject = ProjectFactory.staffPick().toBuilder().category(CategoryFactory.ceramicsCategory()).build();
    final Project artStaffPickProject = ProjectFactory.staffPick().toBuilder().category(CategoryFactory.artCategory()).build();
    final DiscoveryParams allProjects = DiscoveryParams.builder().build();
    final DiscoveryParams artProjects = DiscoveryParams.builder().category(CategoryFactory.artCategory()).build();
    final DiscoveryParams staffPicks = DiscoveryParams.builder().staffPicks(true).build();
    final DiscoveryParams ceramicsProjects = DiscoveryParams.builder().category(CategoryFactory.ceramicsCategory()).build();

    this.vm.inputs.configureWith(Pair.create(artProject, allProjects));
    this.projectTagContainerIsGone.assertValue(false);

    this.vm.inputs.configureWith(Pair.create(artStaffPickProject, allProjects));
    this.projectTagContainerIsGone.assertValue(false);

    this.vm.inputs.configureWith(Pair.create(artProject, artProjects));
    this.projectTagContainerIsGone.assertValues(false, true);

    this.vm.inputs.configureWith(Pair.create(artStaffPickProject, artProjects));
    this.projectTagContainerIsGone.assertValues(false, true, false);

    this.vm.inputs.configureWith(Pair.create(ceramicsProject, artProjects));
    this.projectTagContainerIsGone.assertValues(false, true, false);

    this.vm.inputs.configureWith(Pair.create(ceramicsStaffPickProject, artProjects));
    this.projectTagContainerIsGone.assertValues(false, true, false);

    this.vm.inputs.configureWith(Pair.create(ceramicsStaffPickProject, ceramicsProjects));
    this.projectTagContainerIsGone.assertValues(false, true, false);

    this.vm.inputs.configureWith(Pair.create(ceramicsProject, ceramicsProjects));
    this.projectTagContainerIsGone.assertValues(false, true, false, true);

    this.vm.inputs.configureWith(Pair.create(ceramicsProject, staffPicks));
    this.projectTagContainerIsGone.assertValues(false, true, false, true, false);

    this.vm.inputs.configureWith(Pair.create(ceramicsStaffPickProject, staffPicks));
    this.projectTagContainerIsGone.assertValues(false, true, false, true, false);

    this.vm.inputs.configureWith(Pair.create(artProject, staffPicks));
    this.projectTagContainerIsGone.assertValues(false, true, false, true, false);

    this.vm.inputs.configureWith(Pair.create(artStaffPickProject, staffPicks));
    this.projectTagContainerIsGone.assertValues(false, true, false, true, false);
  }

  @Test
  public void testProjectWeLoveIsGone() {
    setUpEnvironment(environment());

    final Project musicProject = ProjectFactory.project();
    final Project staffPickProject = ProjectFactory.staffPick();
    final DiscoveryParams allProjects = DiscoveryParams.builder().build();
    final DiscoveryParams staffPicks = DiscoveryParams.builder().staffPicks(true).build();

    this.vm.inputs.configureWith(Pair.create(musicProject, allProjects));
    this.projectWeLoveIsGone.assertValue(true);

    this.vm.inputs.configureWith(Pair.create(staffPickProject, allProjects));
    this.projectWeLoveIsGone.assertValues(true, false);

    this.vm.inputs.configureWith(Pair.create(staffPickProject, staffPicks));
    this.projectWeLoveIsGone.assertValues(true, false, true);
  }

  @Test
  public void testRootCategoryNameForFeatured() {
    final Category category = CategoryFactory.bluesCategory();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(category)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.rootCategoryNameForFeatured.assertValues(category.root().name());
  }

  @Test
  public void testSetDefaultTopPadding_noMetaData() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .isBacking(false)
      .isStarred(false)
      .featuredAt(null)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.setDefaultTopPadding.assertValue(true);
  }

  @Test
  public void testSetDefaultTopPadding_withMetaData() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .isBacking(true)
      .isStarred(false)
      .featuredAt(null)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.setDefaultTopPadding.assertValue(false);
  }

  @Test
  public void testStarredViewGroupIsGone_isStarred() {
    final Project project = ProjectFactory.project().toBuilder().isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.savedViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testStarredViewGroupIsGone_isStarred_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, DiscoveryParams.builder().build()));
    this.savedViewGroupIsGone.assertValues(true);
  }
}
