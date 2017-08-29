package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerStatsRowHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel vm;

  private final TestSubscriber<String> percentageOfTotalPledged = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndPledgedForReferrer = new TestSubscriber<>();
  private final TestSubscriber<String> referrerBackerCount = new TestSubscriber<>();
  private final TestSubscriber<String> referrerSourceName = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel(environment);
    this.vm.outputs.percentageOfTotalPledged().subscribe(this.percentageOfTotalPledged);
    this.vm.outputs.projectAndPledgedForReferrer().subscribe(this.projectAndPledgedForReferrer);
    this.vm.outputs.referrerBackerCount().subscribe(this.referrerBackerCount);
    this.vm.outputs.referrerSourceName().subscribe(this.referrerSourceName);
  }

  @Test
  public void testPercentageOfTotalPledged() {
    final Project project = ProjectFactory.project().toBuilder().pledged(100).build();
    final ProjectStatsEnvelope.ReferrerStats referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(50)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, referrerStats));
    this.percentageOfTotalPledged.assertValues("(50%)");
  }

  @Test
  public void testProjectAndPledgedForReferrer() {
    final Project project = ProjectFactory.project().toBuilder().pledged(100).build();
    final ProjectStatsEnvelope.ReferrerStats referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(50)
      .build();

    final float pledgedFloat = (float) referrerStats.pledged();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, referrerStats));
    this.projectAndPledgedForReferrer.assertValues(Pair.create(project, pledgedFloat));
  }

  @Test
  public void testReferrerBackerCount() {
    final ProjectStatsEnvelope.ReferrerStats referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .backersCount(10)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(ProjectFactory.project(), referrerStats));
    this.referrerBackerCount.assertValues(NumberUtils.format(10));
  }

  @Test
  public void testReferrerCode() {
    final ProjectStatsEnvelope.ReferrerStats referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .referrerName("Friends Backed Email")
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(ProjectFactory.project(), referrerStats));
    this.referrerSourceName.assertValues("Friends Backed Email");
  }
}
