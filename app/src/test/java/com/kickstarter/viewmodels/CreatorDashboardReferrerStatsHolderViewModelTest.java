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

import java.util.Arrays;
import java.util.List;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerStatsHolderViewModelTest extends KSRobolectricTestCase  {
  private CreatorDashboardReferrerStatsHolderViewModel.ViewModel vm;

  private final TestSubscriber<List<ProjectStatsEnvelope.ReferrerStats>> referrerStatsOutput = new TestSubscriber<>();
  private final TestSubscriber<Project> projectOutput = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndReferrerStats().map(PairUtils::first).subscribe(projectOutput);
    this.vm.outputs.projectAndReferrerStats().map(PairUtils::second).subscribe(referrerStatsOutput);
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
    this.referrerStatsOutput.assertValues(sortedReferrerList);
  }
}
