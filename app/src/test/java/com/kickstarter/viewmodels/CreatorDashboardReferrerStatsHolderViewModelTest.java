package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerStatsHolderViewModelTest extends KSRobolectricTestCase  {
  private CreatorDashboardReferrerStatsHolderViewModel.ViewModel vm;

  private final TestSubscriber<Project> projectOutput = new TestSubscriber<>();
  private final TestSubscriber<Boolean> referrerStatsListIsGone = new TestSubscriber<>();
  private final TestSubscriber<List<ProjectStatsEnvelope.ReferrerStats>> referrerStatsOutput = new TestSubscriber<>();
  private final TestSubscriber<Boolean> referrerStatsTruncatedTextIsGone = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndReferrerStats().map(PairUtils::first).subscribe(this.projectOutput);
    this.vm.outputs.projectAndReferrerStats().map(PairUtils::second).subscribe(this.referrerStatsOutput);
    this.vm.outputs.referrerStatsListIsGone().subscribe(this.referrerStatsListIsGone);
    this.vm.outputs.referrerStatsTruncatedTextIsGone().subscribe(this.referrerStatsTruncatedTextIsGone);
  }

  @Test
  public void testProjectAndReferrerStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats referrerWithOnePledged = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(1).build();
    final ProjectStatsEnvelope.ReferrerStats referrerWithTwoPledged = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(2).build();
    final ProjectStatsEnvelope.ReferrerStats referrerWithThreePledged = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(3).build();
    final List<ProjectStatsEnvelope.ReferrerStats> unsortedReferrerList = Arrays.asList(referrerWithOnePledged, referrerWithThreePledged, referrerWithTwoPledged);
    final List<ProjectStatsEnvelope.ReferrerStats> sortedReferrerList = Arrays.asList(referrerWithThreePledged, referrerWithTwoPledged, referrerWithOnePledged);
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, unsortedReferrerList));
    this.projectOutput.assertValues(project);
    this.referrerStatsOutput.assertValue(sortedReferrerList);
  }

  @Test
  public void testReferrerStatsListIsGone() {
    setUpEnvironment(environment());

    final Project project = ProjectFactory.project();
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, new ArrayList<>()));

    this.referrerStatsListIsGone.assertValue(true);
    this.referrerStatsTruncatedTextIsGone.assertValue(true);

    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, Collections.singletonList(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats())));
    this.referrerStatsListIsGone.assertValues(true, false);
    this.referrerStatsTruncatedTextIsGone.assertValue(true);
  }

  @Test
  public void testReferrerStatsTruncatedTextIsGone() {
    setUpEnvironment(environment());

    final Project project = ProjectFactory.project();
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, Collections.singletonList(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats())));

    this.referrerStatsTruncatedTextIsGone.assertValue(true);

    final List<ProjectStatsEnvelope.ReferrerStats> maxStats = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      maxStats.add(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(i).build());
    }

    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, maxStats));
    this.referrerStatsTruncatedTextIsGone.assertValues(true);

    maxStats.add(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(11).build());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, maxStats));
    this.referrerStatsTruncatedTextIsGone.assertValues(true, false);
  }
}
