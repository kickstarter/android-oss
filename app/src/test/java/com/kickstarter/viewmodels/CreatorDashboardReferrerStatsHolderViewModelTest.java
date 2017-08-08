package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerStatsHolderViewModelTest extends KSRobolectricTestCase  {
  private CreatorDashboardReferrerStatsHolderViewModel.ViewModel vm;

  private final TestSubscriber<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStats = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndReferrerStats().subscribe(this.projectAndReferrerStats);
  }

  @Test
  public void testProjectAndReferrerStats() {
    final Project project = ProjectFactory.project();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerStatsList = ProjectStatsEnvelopeFactory.ProjectStatsEnvelope().referralDistribution();
    final Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStatsList = Pair.create(project, referrerStatsList);
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReferrerStatsInput(projectAndReferrerStatsList);
    this.projectAndReferrerStats.assertValues(projectAndReferrerStatsList);
  }
}
