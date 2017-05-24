package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.observers.TestSubscriber;


public class ProjectCardholderViewModelTest extends KSRobolectricTestCase {
  private ProjectCardHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> backersCountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backingViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> deadlineCountdownText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> featuredViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> friendBackingViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<String> friendAvatarUrl = new TestSubscriber<>();
  private final TestSubscriber<List<User>> friendsForNamepile = new TestSubscriber<>();
  private final TestSubscriber<Boolean> fundingUnsuccessfulTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> imageIsInvisible = new TestSubscriber<>();
  private final TestSubscriber<Integer> metadataViewGroupBackgroundColor = new TestSubscriber<>();
  private final TestSubscriber<Boolean> metadataViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Project> notifyDelegateOfProjectClick = new TestSubscriber<>();
  private final TestSubscriber<Integer> percentageFunded = new TestSubscriber<>();
  private final TestSubscriber<Boolean> percentageFundedProgressBarIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> percentageFundedTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> photoUrl = new TestSubscriber<>();
  private final TestSubscriber<Boolean> potdViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectCanceledAt = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectFailedAt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> projectStateViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectSuccessfulAt = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectSuspendedAt = new TestSubscriber<>();
  private final TestSubscriber<String> rootCategoryNameForFeatured = new TestSubscriber<>();
  private final TestSubscriber<Boolean> setDefaultTopPadding = new TestSubscriber<>();
  private final TestSubscriber<Boolean> starredViewGroupIsGone = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProjectCardHolderViewModel.ViewModel(environment);
    this.vm.outputs.backersCountTextViewText().subscribe(this.backersCountTextViewText);
    this.vm.outputs.backingViewGroupIsGone().subscribe(this.backingViewGroupIsGone);
    this.vm.outputs.deadlineCountdownText().subscribe(this.deadlineCountdownText);
    this.vm.outputs.featuredViewGroupIsGone().subscribe(this.featuredViewGroupIsGone);
    this.vm.outputs.friendBackingViewIsHidden().subscribe(this.friendBackingViewIsHidden);
    this.vm.outputs.friendAvatarUrl().subscribe(this.friendAvatarUrl);
    this.vm.outputs.friendsForNamepile().subscribe(this.friendsForNamepile);
    this.vm.outputs.fundingUnsuccessfulTextViewIsGone().subscribe(this.fundingUnsuccessfulTextViewIsGone);
    this.vm.outputs.imageIsInvisible().subscribe(this.imageIsInvisible);
    this.vm.outputs.metadataViewGroupBackgroundColor().subscribe(this.metadataViewGroupBackgroundColor);
    this.vm.outputs.metadataViewGroupIsGone().subscribe(this.metadataViewGroupIsGone);
    this.vm.outputs.notifyDelegateOfProjectClick().subscribe(this.notifyDelegateOfProjectClick);
    this.vm.outputs.percentageFunded().subscribe(this.percentageFunded);
    this.vm.outputs.percentageFundedProgressBarIsGone().subscribe(this.percentageFundedProgressBarIsGone);
    this.vm.outputs.percentageFundedTextViewText().subscribe(this.percentageFundedTextViewText);
    this.vm.outputs.photoUrl().subscribe(this.photoUrl);
    this.vm.outputs.potdViewGroupIsGone().subscribe(this.potdViewGroupIsGone);
    this.vm.outputs.projectCanceledAt().subscribe(this.projectCanceledAt);
    this.vm.outputs.projectFailedAt().subscribe(this.projectFailedAt);
    this.vm.outputs.projectStateViewGroupIsGone().subscribe(this.projectStateViewGroupIsGone);
    this.vm.outputs.projectSuccessfulAt().subscribe(this.projectSuccessfulAt);
    this.vm.outputs.projectSuspendedAt().subscribe(this.projectSuspendedAt);
    this.vm.outputs.rootCategoryNameForFeatured().subscribe(this.rootCategoryNameForFeatured);
    this.vm.outputs.setDefaultTopPadding().subscribe(this.setDefaultTopPadding);
    this.vm.outputs.starredViewGroupIsGone().subscribe(this.starredViewGroupIsGone);
  }

  @Test
  public void testEmitsBackersCountTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(50).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.backersCountTextViewText.assertValues(NumberUtils.format(50));
  }

  @Test
  public void testBackingViewGroupIsGone_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.backingViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testBackingViewGroupIsGone_isStarred() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .isBacking(false)
      .isStarred(false)
      .potdAt(null)
      .featuredAt(null)
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.backingViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testEmitsDeadlineCountdownText() {
    final Project project = ProjectFactory.project().toBuilder()
      .deadline(new DateTime().plusSeconds(60 * 60 * 24 + 1))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.deadlineCountdownText.assertValues("24");
  }

  @Test
  public void testEmitsPhotoUrl() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.photoUrl.assertValues(project.photo().full());
  }

  @Test
  public void testFeaturedViewGroupIsGone_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.featuredViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testFeaturedViewGroupIsGone_isFeatured() {
    final Project project = ProjectFactory.project().toBuilder().featuredAt(DateTime.now()).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.featuredViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testFriendAvatarUrl() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.friendAvatarUrl.assertValues(project.friends().get(0).avatar().small());
  }

  @Test
  public void testFriendBackingViewIsNotHidden() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);

    // friends view is not hidden for project with friend backings
    this.friendBackingViewIsHidden.assertValues(false);
  }

  @Test
  public void testEmitsFriendBackingViewIsHidden() {
    final Project project = ProjectFactory.project().toBuilder().friends(null).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.friendBackingViewIsHidden.assertValues(true);
  }

  @Test
  public void testFriendsForNamepile() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .friends(Collections.singletonList(UserFactory.user()))
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.friendsForNamepile.assertValues(project.friends());
  }

  @Test
  public void testFundingUnsuccessfulTextViewIsGone_projectLive() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.fundingUnsuccessfulTextViewIsGone.assertValues(true);
  }

  @Test
  public void testFundingUnsuccessfulTextViewIsGone_projectFailed() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_FAILED).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.fundingUnsuccessfulTextViewIsGone.assertValues(false);
  }

  @Test
  public void testEmitsImageIsInvisible() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.imageIsInvisible.assertValues(ObjectUtils.isNull(project.photo()));
  }

  @Test
  public void testMetadataViewGroupBackgroundColor() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.metadataViewGroupBackgroundColor.assertValues(R.color.ksr_green_500);
  }

  @Test
  public void testEmitsMetadataViewGroupIsGone() {
    final Project project = ProjectFactory.project().toBuilder().isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.metadataViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testNotifyDelegateOfProjectNameClick() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.vm.inputs.projectClicked();
    this.notifyDelegateOfProjectClick.assertValues(project);
  }

  @Test
  public void testPercentageFunded() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.percentageFunded.assertValues(ProgressBarUtils.progress(project.percentageFunded()));
  }

  @Test
  public void testPercentageFundedProgressBarIsGone_projectSuccessful() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.percentageFundedProgressBarIsGone.assertValues(true);
  }

  @Test
  public void testPercentageFundedProgressBarIsGone_projectLive() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.percentageFundedProgressBarIsGone.assertValues(false);
  }

  @Test
  public void testPercentageFundedTextViewText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.percentageFundedTextViewText.assertValues(NumberUtils.flooredPercentage(project.percentageFunded()));
  }

  @Test
  public void testPotdViewGroupIsGone_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.potdViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testPotdViewGroupIsGone() {
    final Project project = ProjectFactory.project().toBuilder()
      .isBacking(false)
      .isStarred(false)
      .potdAt(DateTime.now())
      .featuredAt(null)
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.potdViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testProjectCanceledAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_CANCELED)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.projectCanceledAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testProjectFailedAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_FAILED)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.projectFailedAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testProjectStateViewGroupIsGone_projectLive() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.projectStateViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testProjectStateViewGroupIsGone_projectSuccessful() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.projectStateViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testProjectSuccessfulAt() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .stateChangedAt(new DateTime().now())
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
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

    this.vm.inputs.configureWith(project);
    this.projectSuspendedAt.assertValues(project.stateChangedAt());
  }

  @Test
  public void testRootCategoryNameForFeatured() {
    final Category category = CategoryFactory.bluesCategory();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .category(category)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.rootCategoryNameForFeatured.assertValues(category.root().name());
  }

  @Test
  public void testSetDefaultTopPadding_noMetaData() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .isBacking(false)
      .isStarred(false)
      .potdAt(null)
      .featuredAt(null)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.setDefaultTopPadding.assertValue(true);
  }

  @Test
  public void testSetDefaultTopPadding_withMetaData() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .isBacking(true)
      .isStarred(false)
      .potdAt(null)
      .featuredAt(null)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.setDefaultTopPadding.assertValue(false);
  }

  @Test
  public void testStarredViewGroupIsGone_isStarred() {
    final Project project = ProjectFactory.project().toBuilder().isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.starredViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testStarredViewGroupIsGone_isStarred_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.starredViewGroupIsGone.assertValues(true);
  }

  @Test
  public void testSuccessfullyFundedTextViewIsGone_projectSuccessful() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_SUCCESSFUL).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.projectStateViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testSuccessfullyFundedTextViewIsGone_projectFailed() {
    final Project project = ProjectFactory.project().toBuilder().state(Project.STATE_FAILED).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.projectStateViewGroupIsGone.assertValues(false);
  }
}
