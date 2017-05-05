package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Collections;

import rx.observers.TestSubscriber;


public class ProjectCardholderViewModelTest extends KSRobolectricTestCase {
  ProjectCardHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> backersCountText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backingViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> blurbText = new TestSubscriber<>();
  private final TestSubscriber<String> categoryNameText = new TestSubscriber<>();
  private final TestSubscriber<String> deadlineCountdownText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> featuredViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> friendBackingViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> imageIsInvisible = new TestSubscriber<>();
  private final TestSubscriber<Boolean> metadataViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> nameText = new TestSubscriber<>();
  private final TestSubscriber<Integer> percentageFunded = new TestSubscriber<>();
  private final TestSubscriber<String> percentageFundedText = new TestSubscriber<>();
  private final TestSubscriber<String> photoUrl = new TestSubscriber<>();
  private final TestSubscriber<Boolean> potdViewGroupIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> starredViewGroupIsGone = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProjectCardHolderViewModel.ViewModel(environment);
    this.vm.outputs.backersCountText().subscribe(this.backersCountText);
    this.vm.outputs.backingViewGroupIsGone().subscribe(this.backingViewGroupIsGone);
    this.vm.outputs.blurbText().subscribe(this.blurbText);
    this.vm.outputs.categoryNameText().subscribe(this.categoryNameText);
    this.vm.outputs.deadlineCountdownText().subscribe(this.deadlineCountdownText);
    this.vm.outputs.featuredViewGroupIsGone().subscribe(this.featuredViewGroupIsGone);
    this.vm.outputs.friendBackingViewIsHidden().subscribe(this.friendBackingViewIsHidden);
    this.vm.outputs.imageIsInvisible().subscribe(this.imageIsInvisible);
    this.vm.outputs.metadataViewGroupIsGone().subscribe(this.metadataViewGroupIsGone);
    this.vm.outputs.nameText().subscribe(this.nameText);
    this.vm.outputs.percentageFunded().subscribe(this.percentageFunded);
    this.vm.outputs.percentageFundedText().subscribe(this.percentageFundedText);
    this.vm.outputs.photoUrl().subscribe(this.photoUrl);
    this.vm.outputs.potdViewGroupIsGone().subscribe(this.potdViewGroupIsGone);
    this.vm.outputs.starredViewGroupIsGone().subscribe(this.starredViewGroupIsGone);
  }

  @Test
  public void testEmitsBackersCount() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(50).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.backersCountText.assertValues(NumberUtils.format(50));
  }

  @Test
  public void testBackingViewGroupIsGone_isBacking() {
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.backingViewGroupIsGone.assertValues(true);
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
    this.backingViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testEmitsCategoryNameText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.categoryNameText.assertValues(project.category().name());
  }

  @Test
  public void testEmitsDeadlineCountdownText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.deadlineCountdownText.assertValues(NumberUtils.format(ProjectUtils.deadlineCountdownValue(project)));
  }

  @Test
  public void testEmitsBlurbText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.blurbText.assertValues(project.blurb());
  }

  @Test
  public void testEmitsPhotoUrl() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.photoUrl.assertValues(project.photo().med());
  }

  @Test
  public void testEmitsImageIsInvisible() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.imageIsInvisible.assertValues(ObjectUtils.isNull(project.photo()));
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
  public void testEmitsMetadataViewGroupIsGone() {
    final Project project = ProjectFactory.project().toBuilder().isStarred(true).build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.metadataViewGroupIsGone.assertValues(false);
  }

  @Test
  public void testEmitsNameText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.nameText.assertValues(project.name());
  }

  @Test
  public void testPercentageFunded() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.percentageFunded.assertValues(ProgressBarUtils.progress(project.percentageFunded()));
  }

  @Test
  public void testPercentageFundedText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(project);
    this.percentageFundedText.assertValues(NumberUtils.flooredPercentage(project.percentageFunded()));
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
}
