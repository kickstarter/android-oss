package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ConfigFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FeatureKey;
import com.kickstarter.libs.MockCurrentConfig;
import com.kickstarter.libs.ReferrerType;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerBreakdownHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel vm;

  private final TestSubscriber<Boolean> breakdownViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> emptyViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> titleViewIsGone = new TestSubscriber<>();
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
    this.vm.outputs.titleViewIsGone().subscribe(this.titleViewIsGone);
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
  public void testBreakdownViewIsGone_isTrue_whenStatsEmptyAndEnabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getEmptyProjectStatsEnvelope();

    setUpEnvironmentWithBreakdownFlagEnabled();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.breakdownViewIsGone.assertValues(true);
  }

  @Test
  public void testBreakdownViewIsGone_isTrue_whenStatsEmptyAndDisabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getEmptyProjectStatsEnvelope();

    setUpEnvironmentWithDefaultConfig();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.breakdownViewIsGone.assertValues(true);
  }

  @Test
  public void testBreakdownViewIsGone_isTrue_whenStatsNotEmptyAndDisabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getProjectStatsEnvelope();

    setUpEnvironmentWithDefaultConfig();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.breakdownViewIsGone.assertValues(true);
  }

  @Test
  public void testBreakdownViewIsGone_isFalse_whenStatsNotEmptyAndEnabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getProjectStatsEnvelope();

    setUpEnvironmentWithBreakdownFlagEnabled();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.breakdownViewIsGone.assertValues(false);
  }

  @Test
  public void testEmptyViewIsGone_isFalse_whenStatsEmptyAndEnabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getEmptyProjectStatsEnvelope();

    setUpEnvironmentWithBreakdownFlagEnabled();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.emptyViewIsGone.assertValues(false);
  }

  @Test
  public void testEmptyViewIsGone_isTrue_whenStatsEmptyAndDisabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getEmptyProjectStatsEnvelope();

    setUpEnvironmentWithDefaultConfig();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.emptyViewIsGone.assertValues(true);
  }

  @Test
  public void testEmptyViewIsGone_isTrue_whenStatsNotEmptyAndDisabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getProjectStatsEnvelope();

    setUpEnvironmentWithDefaultConfig();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.emptyViewIsGone.assertValues(true);
  }

  @Test
  public void testEmptyViewIsGone_isTrue_whenStatsNotEmptyAndEnabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getProjectStatsEnvelope();

    setUpEnvironmentWithBreakdownFlagEnabled();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.emptyViewIsGone.assertValues(true);
  }

  @Test
  public void testTitleViewIsGone_isFalse_whenEnabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getProjectStatsEnvelope();

    setUpEnvironmentWithBreakdownFlagEnabled();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.titleViewIsGone.assertValues(false);
  }

  @Test
  public void testTitleViewIsGone_isTrue_whenDisabled() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope statsEnvelope = getProjectStatsEnvelope();

    setUpEnvironmentWithDefaultConfig();

    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.titleViewIsGone.assertValues(true);
  }

  @Test
  public void testCustomReferrerPercent() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(55f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fiftyFivePercentCustomReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.customReferrerPercent.assertValues(55f);
  }

  @Test
  public void testCustomReferrerPercentText() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(.55f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fiftyFivePercentCustomReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.customReferrerPercentText.assertValues("55%");
  }

  @Test
  public void testExternalReferrerPercent() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(15f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fifteenPercentExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.externalReferrerPercent.assertValues(15f);
  }

  @Test
  public void testExternalReferrerPercentText() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(15f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fifteenPercentExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.externalReferrerPercentText.assertValues(NumberUtils.flooredPercentage(15f * 100f));
  }

  @Test
  public void testKickstarterReferrerPercent() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentKickstarterReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(30f)
      .referrerType(ReferrerType.KICKSTARTER.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(thirtyPercentKickstarterReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.kickstarterReferrerPercent.assertValues(30f);
  }

  @Test
  public void testKickstarterReferrerPercentText() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentKickstarterReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(30f)
      .referrerType(ReferrerType.KICKSTARTER.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(thirtyPercentKickstarterReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.kickstarterReferrerPercentText.assertValues(NumberUtils.flooredPercentage(30f * 100f));
  }

  @Test
  public void testPledgedViaCustomLayoutIsGone() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats zeroPledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(0f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(zeroPledgedCustomReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.pledgedViaCustomLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaExternalLayoutIsGone() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats zeroPledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(0f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(zeroPledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.pledgedViaExternalLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaKickstarterLayoutIsGone() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats zeroPledgedKickstarterReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(0f)
      .referrerType(ReferrerType.KICKSTARTER.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(zeroPledgedKickstarterReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.pledgedViaKickstarterLayoutIsGone.assertValues(true);
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
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats onePledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(1f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats twoPledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(2f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats threePledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(3f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      onePledgedCustomReferrer, twoPledgedCustomReferrer, threePledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndCustomReferrerPledgedAmount.assertValue(Pair.create(project, 3f));
  }

  @Test
  public void testProjectAndExternalReferrerPledgedAmount() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats onePledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(1f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats twoPledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(2f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats threePledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(3f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      onePledgedCustomReferrer, twoPledgedCustomReferrer, threePledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndExternalReferrerPledgedAmount.assertValue(Pair.create(project, 3f));
  }

  @Test
  public void testProjectAndKickstarterReferrerPledgedAmount() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats onePledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(1f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats twoPledgedKickstarterReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(2f)
      .referrerType(ReferrerType.KICKSTARTER.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats threePledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(3f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      onePledgedCustomReferrer, twoPledgedKickstarterReferrer, threePledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndKickstarterReferrerPledgedAmount.assertValue(Pair.create(project, 2f));
  }

  @Test
  public void testReferrerPercents() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(55f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(15f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats tenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(10f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentKickstarterReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .percentageOfDollars(30f)
      .referrerType(ReferrerType.KICKSTARTER.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      fifteenPercentExternalReferrer,
      tenPercentExternalReferrer,
      thirtyPercentKickstarterReferrer,
      fiftyFivePercentCustomReferrer);
    setUpEnvironment(environment());
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.customReferrerPercent.assertValues(55.0f);
    this.externalReferrerPercent.assertValues(25.0f);
    this.kickstarterReferrerPercent.assertValues(30.0f);
  }

  private ProjectStatsEnvelope getEmptyProjectStatsEnvelope() {
    return ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(ListUtils.empty())
      .build();
  }

  private ProjectStatsEnvelope getProjectStatsEnvelope() {
    final ProjectStatsEnvelope.ReferrerStats referrerStat = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(referrerStat);
    return ProjectStatsEnvelopeFactory.projectStatsEnvelope()
      .toBuilder()
      .referralDistribution(referrerList)
      .build();
  }

  private void setUpEnvironmentWithBreakdownFlagEnabled() {
    final CurrentConfigType currentConfig = new MockCurrentConfig();
    currentConfig.config(ConfigFactory.configWithFeatureEnabled(FeatureKey.NATIVE_CREATOR_BREAKDOWN_CHART));

    final Environment environment = environment()
      .toBuilder()
      .currentConfig(currentConfig)
      .build();
    setUpEnvironment(environment);
  }

  private void setUpEnvironmentWithDefaultConfig() {
    final CurrentConfigType currentConfig = new MockCurrentConfig();
    currentConfig.config(ConfigFactory.config());

    final Environment environment = environment()
      .toBuilder()
      .currentConfig(currentConfig)
      .build();
    setUpEnvironment(environment);
  }
}
