package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerStatsHolderViewModelTest extends KSRobolectricTestCase  {
  private CreatorDashboardReferrerStatsHolderViewModel.ViewModel vm;

  private final TestSubscriber<Project> projectOutput = new TestSubscriber<>();
  private final TestSubscriber<Boolean> referrerStatsListIsGone = new TestSubscriber<>();
  private final TestSubscriber<List<ProjectStatsEnvelope.ReferrerStats>> referrerStatsOutput = new TestSubscriber<>();
  private final TestSubscriber<Boolean> referrersTitleIsLimitedCopy = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndReferrerStats().map(PairUtils::first).subscribe(this.projectOutput);
    this.vm.outputs.projectAndReferrerStats().map(PairUtils::second).subscribe(this.referrerStatsOutput);
    this.vm.outputs.referrerStatsListIsGone().subscribe(this.referrerStatsListIsGone);
    this.vm.outputs.referrersTitleIsTopTen().subscribe(this.referrersTitleIsLimitedCopy);
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
    this.referrersTitleIsLimitedCopy.assertValue(false);

    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, Collections.singletonList(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats())));
    this.referrerStatsListIsGone.assertValues(true, false);
    this.referrersTitleIsLimitedCopy.assertValue(false);
  }

  @Test
  public void testReferrersTitleIsLimitedCopy() {
    setUpEnvironment(environment());

    final Project project = ProjectFactory.project();
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, Collections.singletonList(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats())));

    this.referrersTitleIsLimitedCopy.assertValue(false);

    final List<ProjectStatsEnvelope.ReferrerStats> maxStats = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      maxStats.add(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(i).build());
    }

    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, maxStats));
    this.referrersTitleIsLimitedCopy.assertValues(false);

    maxStats.add(ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().pledged(11).build());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, maxStats));
    this.referrersTitleIsLimitedCopy.assertValues(false, true);
  }
}
