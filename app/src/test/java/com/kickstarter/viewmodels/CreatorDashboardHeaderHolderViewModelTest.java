package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.joda.time.DateTime;
import org.junit.Test;
import rx.observers.TestSubscriber;

public class CreatorDashboardHeaderHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardHeaderHolderViewModel.ViewModel vm;

  private final TestSubscriber<String> percentageFunded = new TestSubscriber<>();
  private final TestSubscriber<String> projectBackersCountText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> timeRemainingText = new TestSubscriber<>();
  private final TestSubscriber<Project> startMessageThreadsActivity = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectBackersCountText().subscribe(this.projectBackersCountText);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.percentageFunded().subscribe(this.percentageFunded);
    this.vm.outputs.startMessageThreadsActivity().subscribe(this.startMessageThreadsActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.timeRemainingText().subscribe(this.timeRemainingText);
  }

  @Test
  public void testProjectBackersCountText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(10).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.projectBackersCountText.assertValues("10");
  }

  @Test
  public void testProjectNameTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().name("somebody once told me").build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.projectNameTextViewText.assertValues("somebody once told me");
  }

  @Test
  public void testPercentageFunded() {
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    final String percentageFundedOutput = NumberUtils.flooredPercentage(project.percentageFunded());
    this.percentageFunded.assertValues(percentageFundedOutput);
  }

  @Test
  public void testStartMessagesActivity() {
    final Project project = ProjectFactory.project();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, ProjectStatsEnvelopeFactory.projectStatsEnvelope()));

    this.vm.inputs.messagesButtonClicked();
    this.startMessageThreadsActivity.assertValues(project);
  }

  @Test
  public void testStartProjectActivity() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.vm.inputs.viewProjectButtonClicked();
    this.startProjectActivity.assertValues(Pair.create(project, RefTag.dashboard()));
  }

  @Test
  public void testTimeRemainingText() {
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project().toBuilder().deadline(new DateTime().plusDays(10)).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();
    final int deadlineVal = ProjectUtils.deadlineCountdownValue(project);

    this.vm.inputs.projectAndStats(Pair.create(project, projectStatsEnvelope));
    this.timeRemainingText.assertValues(NumberUtils.format(deadlineVal));
  }
}
