package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;

import org.joda.time.DateTime;
import org.junit.Test;
import rx.observers.TestSubscriber;

public class CreatorDashboardHeaderHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardHeaderHolderViewModel.ViewModel vm;

  private final TestSubscriber<String> percentageFunded = new TestSubscriber<>();
  private final TestSubscriber<String> projectBackersCountText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> timeRemainingText = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment);
  }

  @Test
  public void testProjectBackersCountText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(10).build();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.projectBackersCountText().subscribe(this.projectBackersCountText);
    this.vm.inputs.projectAndStats(project, projectStats);
    this.projectBackersCountText.assertValues("10");
  }

  @Test
  public void testProjectNameTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().name("somebody once told me").build();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.inputs.projectAndStats(project, projectStats);
    this.projectNameTextViewText.assertValues("somebody once told me");
  }

  @Test
  public void testPercentageFunded() {
    final Project project = ProjectFactory.project();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.percentageFunded().subscribe(this.percentageFunded);
    this.vm.inputs.projectAndStats(project, projectStats);
    final String percentageFundedOutput = NumberUtils.flooredPercentage(project.percentageFunded());
    this.percentageFunded.assertValues(percentageFundedOutput);
  }

  @Test
  public void testStartProjectActivity() {
    final Project project = ProjectFactory.project();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.inputs.projectAndStats(project, projectStats);
    this.vm.inputs.projectViewClicked();
    this.startProjectActivity.assertValues(Pair.create(project, RefTag.dashboard()));
  }

  @Test
  public void testTimeRemainingText() {
    final Project project = ProjectFactory.project().toBuilder().deadline(new DateTime().plusDays(10)).build();
    final ProjectStats projectStats = ProjectStatsFactory.projectStats();
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    this.vm.outputs.timeRemainingText().subscribe(this.timeRemainingText);
    this.vm.inputs.projectAndStats(project, projectStats);
    final int deadlineVal = ProjectUtils.deadlineCountdownValue(project);
    this.timeRemainingText.assertValues(NumberUtils.format(deadlineVal));
  }
}
