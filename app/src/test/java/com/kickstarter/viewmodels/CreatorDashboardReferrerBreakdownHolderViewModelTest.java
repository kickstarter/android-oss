package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerBreakdownHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel vm;

  private final TestSubscriber<Boolean> breakdownViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> emptyViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Float> customReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<Float> externalReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<Float> kickstarterReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<String> customReferrerPercentText = new TestSubscriber<>();
  private final TestSubscriber<String> externalReferrerPercentText = new TestSubscriber<>();
  private final TestSubscriber<String> kickstarterReferrerPercentText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> pledgedViaCustomLayoutIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> pledgedViaExternalLayoutIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> pledgedViaKickstarterLayoutIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Integer>> projectAndAveragePledge = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndKickstarterReferrerPledgedAmount = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment);
    this.vm.outputs.breakdownViewIsGone().subscribe(this.breakdownViewIsGone);
    this.vm.outputs.emptyViewIsGone().subscribe(this.emptyViewIsGone);
    this.vm.outputs.customReferrerPercent().subscribe(this.customReferrerPercent);
    this.vm.outputs.customReferrerPercentText().subscribe(this.customReferrerPercentText);
    this.vm.outputs.externalReferrerPercent().subscribe(this.externalReferrerPercent);
    this.vm.outputs.externalReferrerPercentText().subscribe(this.externalReferrerPercentText);
    this.vm.outputs.kickstarterReferrerPercent().subscribe(this.kickstarterReferrerPercent);
    this.vm.outputs.kickstarterReferrerPercentText().subscribe(this.kickstarterReferrerPercentText);
    this.vm.outputs.pledgedViaCustomLayoutIsGone().subscribe(this.pledgedViaCustomLayoutIsGone);
    this.vm.outputs.pledgedViaExternalLayoutIsGone().subscribe(this.pledgedViaExternalLayoutIsGone);
    this.vm.outputs.pledgedViaKickstarterLayoutIsGone().subscribe(this.pledgedViaKickstarterLayoutIsGone);
    this.vm.outputs.projectAndAveragePledge().subscribe(this.projectAndAveragePledge);
    this.vm.outputs.projectAndCustomReferrerPledgedAmount().subscribe(this.projectAndCustomReferrerPledgedAmount);
    this.vm.outputs.projectAndExternalReferrerPledgedAmount().subscribe(this.projectAndExternalReferrerPledgedAmount);
    this.vm.outputs.projectAndKickstarterReferrerPledgedAmount()
      .subscribe(this.projectAndKickstarterReferrerPledgedAmount);
  }

  @Test
  public void testBreakdownViewIsGone_isTrue_whenStatsEmpty() {
    setUpEnvironmentAndInputProjectAndEmptyStats();
    this.breakdownViewIsGone.assertValues(true);
  }

  @Test
  public void testBreakdownViewIsGone_isFalse_whenStatsNotEmpty() {
    setUpEnvironmentAndInputProjectAndStats();
    this.breakdownViewIsGone.assertValues(false);
  }

  @Test
  public void testEmptyViewIsGone_isFalse_whenStatsEmpty() {
    setUpEnvironmentAndInputProjectAndEmptyStats();
    this.emptyViewIsGone.assertValues(false);
  }

  @Test
  public void testEmptyViewIsGone_isTrue_whenStatsNotEmpty() {
    setUpEnvironmentAndInputProjectAndStats();
    this.emptyViewIsGone.assertValues(true);
  }

  @Test
  public void testCustomReferrerPercent() {
    setUpEnvironmentAndInputProjectAndStats();
    this.customReferrerPercent.assertValues(.5f);
  }

  @Test
  public void testCustomReferrerPercentText() {
    setUpEnvironmentAndInputProjectAndStats();
    this.customReferrerPercentText.assertValues(NumberUtils.flooredPercentage(.5f * 100f));
  }

  @Test
  public void testExternalReferrerPercent() {
    setUpEnvironmentAndInputProjectAndStats();
    this.externalReferrerPercent.assertValues(.25f);
  }

  @Test
  public void testExternalReferrerPercentText() {
    setUpEnvironmentAndInputProjectAndStats();
    this.externalReferrerPercentText.assertValues(NumberUtils.flooredPercentage(.25f * 100f));
  }

  @Test
  public void testKickstarterReferrerPercent() {
    setUpEnvironmentAndInputProjectAndStats();
    this.kickstarterReferrerPercent.assertValues(.25f);
  }

  @Test
  public void testKickstarterReferrerPercentText() {
    setUpEnvironmentAndInputProjectAndStats();
    this.kickstarterReferrerPercentText.assertValues(NumberUtils.flooredPercentage(.25f * 100f));
  }

  @Test
  public void testPledgedViaCustomLayoutIsGone_isTrue_WhenStatsEmpty() {
    setUpEnvironmentAndInputProjectAndEmptyStats();
    this.pledgedViaCustomLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaCustomLayoutIsGone_isFalse_WhenStatsNotEmpty() {
    setUpEnvironmentAndInputProjectAndStats();
    this.pledgedViaCustomLayoutIsGone.assertValues(false);
  }

  @Test
  public void testPledgedViaExternalLayoutIsGone_isTrue_WhenStatsEmpty() {
    setUpEnvironmentAndInputProjectAndEmptyStats();
    this.pledgedViaExternalLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaExternalLayoutIsGone_isFalse_WhenStatsNotEmpty() {
    setUpEnvironmentAndInputProjectAndStats();
    this.pledgedViaExternalLayoutIsGone.assertValues(false);
  }

  @Test
  public void testPledgedViaKickstarterLayoutIsGone_isTrue_WhenStatsEmpty() {
    setUpEnvironmentAndInputProjectAndEmptyStats();
    this.pledgedViaKickstarterLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaKickstarterLayoutIsGone_isFalse_WhenStatsNotEmpty() {
    setUpEnvironmentAndInputProjectAndStats();
    this.pledgedViaKickstarterLayoutIsGone.assertValues(false);
  }

  @Test
  public void testProjectAndAveragePledge() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.CumulativeStats cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()
      .toBuilder()
      .averagePledge(10f)
      .build();

    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .cumulative(cumulativeStats)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndAveragePledge.assertValue(Pair.create(project, 10));
  }

  @Test
  public void testProjectAndCustomReferrerPledgedAmount() {
    final Project project = setUpEnvironmentAndInputProjectAndStats();
    this.projectAndCustomReferrerPledgedAmount.assertValue(Pair.create(project, 100f));
  }

  @Test
  public void testProjectAndExternalReferrerPledgedAmount() {
    final Project project = setUpEnvironmentAndInputProjectAndStats();
    this.projectAndExternalReferrerPledgedAmount.assertValue(Pair.create(project, 50f));
  }

  @Test
  public void testProjectAndKickstarterReferrerPledgedAmount() {
    final Project project = setUpEnvironmentAndInputProjectAndStats();
    this.projectAndKickstarterReferrerPledgedAmount.assertValue(Pair.create(project, 50f));
  }

  @Test
  public void testReferrerPercents() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferralAggregateStats referralAggregateStats = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory
      .referralAggregates()
      .toBuilder()
      .custom(100)
      .internal(50)
      .external(50)
      .build();

    ProjectStatsEnvelope.CumulativeStats cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()
      .toBuilder()
      .pledged(200)
      .build();

    ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder()
      .referralAggregates(referralAggregateStats)
      .cumulative(cumulativeStats)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, projectStatsEnvelope));

    this.customReferrerPercent.assertValues(.5f);
    this.externalReferrerPercent.assertValues(.25f);
    this.kickstarterReferrerPercent.assertValues(.25f);
  }

  private @NonNull Project setUpEnvironmentAndInputProjectAndStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = getProjectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, projectStatsEnvelope));
    return project;
  }

  private @NonNull Project setUpEnvironmentAndInputProjectAndEmptyStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = getEmptyProjectStatsEnvelope();

    setUpEnvironment(environment());

    this.vm.inputs.projectAndStatsInput(Pair.create(project, projectStatsEnvelope));
    return project;
  }

  private ProjectStatsEnvelope getEmptyProjectStatsEnvelope() {
    ProjectStatsEnvelope.ReferralAggregateStats referralAggregateStats = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()
      .toBuilder()
      .custom(0)
      .external(0)
      .internal(0)
      .build();

    ProjectStatsEnvelope.CumulativeStats cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()
      .toBuilder()
      .pledged(0)
      .build();

    return ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .cumulative(cumulativeStats)
      .referralAggregates(referralAggregateStats)
      .referralDistribution(ListUtils.empty())
      .build();
  }

  private ProjectStatsEnvelope getProjectStatsEnvelope() {
    final ProjectStatsEnvelope.ReferralAggregateStats referralAggregateStats = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory
      .referralAggregates()
      .toBuilder()
      .custom(100)
      .internal(50)
      .external(50)
      .build();

    ProjectStatsEnvelope.CumulativeStats cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()
      .toBuilder()
      .pledged(200)
      .build();

    return ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder()
      .referralAggregates(referralAggregateStats)
      .cumulative(cumulativeStats)
      .build();
  }
}
