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

public class CreatorDashboardRewardStatsHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardRewardStatsHolderViewModel.ViewModel vm;

  private final TestSubscriber<Project> projectOutput= new TestSubscriber<>();
  private final TestSubscriber<List<ProjectStatsEnvelope.RewardStats>> rewardStatsOutput = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndRewardStats().map(PairUtils::first).subscribe(this.projectOutput);
    this.vm.outputs.projectAndRewardStats().map(PairUtils::second).subscribe(this.rewardStatsOutput);
  }

  @Test
  public void testProjectAndRewardStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.RewardStats rewardWithOneBacker = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats().toBuilder().backersCount(1).build();
    final ProjectStatsEnvelope.RewardStats rewardWithTwoBackers = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats().toBuilder().backersCount(2).build();
    final ProjectStatsEnvelope.RewardStats rewardWithThreeBackers = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats().toBuilder().backersCount(3).build();
    final List<ProjectStatsEnvelope.RewardStats> unsortedRewardStatsList = Arrays.asList(rewardWithTwoBackers, rewardWithOneBacker, rewardWithThreeBackers);
    final List<ProjectStatsEnvelope.RewardStats> sortedRewardStatsList = Arrays.asList(rewardWithThreeBackers, rewardWithTwoBackers, rewardWithOneBacker);
    setUpEnvironment(environment());

    this.vm.inputs.projectAndRewardStatsInput(Pair.create(project, unsortedRewardStatsList));
    this.projectOutput.assertValues(project);
    this.rewardStatsOutput.assertValues(sortedRewardStatsList);
  }
}
