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

public class CreatorDashboardRewardStatsHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardRewardStatsHolderViewModel.ViewModel vm;

  private final TestSubscriber<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndRewardStats().subscribe(this.projectAndRewardStats);
  }

  @Test
  public void testProjectAndRewardStats() {
    final Project project = ProjectFactory.project();
    final List<ProjectStatsEnvelope.RewardStats> rewardStatsList = ProjectStatsEnvelopeFactory.ProjectStatsEnvelope().rewardDistribution();
    final Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStatsList = Pair.create(project, rewardStatsList);
    setUpEnvironment(environment());

    this.vm.inputs.projectAndRewardStatsInput(projectAndRewardStatsList);
    this.projectAndRewardStats.assertValues(projectAndRewardStatsList);
  }
}
