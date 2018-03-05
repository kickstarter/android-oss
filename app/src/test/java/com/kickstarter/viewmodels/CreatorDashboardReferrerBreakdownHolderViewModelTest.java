package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ReferrerType;
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

  private final TestSubscriber<Float> customReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<Float> externalReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<Float> internalReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<String> customReferrerPercentText = new TestSubscriber<>();
  private final TestSubscriber<String> externalReferrerPercentText = new TestSubscriber<>();
  private final TestSubscriber<String> internalReferrerPercentText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> pledgedViaCustomLayoutIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> pledgedViaExternalLayoutIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> pledgedViaInternalLayoutIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Integer>> projectAndAveragePledge = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Float>> projectAndInternalReferrerPledgedAmount = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment);
    this.vm.outputs.customReferrerPercent().subscribe(this.customReferrerPercent);
    this.vm.outputs.customReferrerPercentText().subscribe(this.customReferrerPercentText);
    this.vm.outputs.externalReferrerPercent().subscribe(this.externalReferrerPercent);
    this.vm.outputs.externalReferrerPercentText().subscribe(this.externalReferrerPercentText);
    this.vm.outputs.internalReferrerPercent().subscribe(this.internalReferrerPercent);
    this.vm.outputs.internalReferrerPercentText().subscribe(this.internalReferrerPercentText);
    this.vm.outputs.pledgedViaCustomLayoutIsGone().subscribe(this.pledgedViaCustomLayoutIsGone);
    this.vm.outputs.pledgedViaExternalLayoutIsGone().subscribe(this.pledgedViaExternalLayoutIsGone);
    this.vm.outputs.pledgedViaInternalLayoutIsGone().subscribe(this.pledgedViaInternalLayoutIsGone);
    this.vm.outputs.projectAndAveragePledge().subscribe(this.projectAndAveragePledge);
    this.vm.outputs.projectAndCustomReferrerPledgedAmount().subscribe(this.projectAndCustomReferrerPledgedAmount);
    this.vm.outputs.projectAndExternalReferrerPledgedAmount().subscribe(this.projectAndExternalReferrerPledgedAmount);
    this.vm.outputs.projectAndInternalReferrerPledgedAmount().subscribe(this.projectAndInternalReferrerPledgedAmount);
  }

  @Test
  public void testCustomReferrerPercent() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(55f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fiftyFivePercentCustomReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.customReferrerPercent.assertValues(55f);
  }

  @Test
  public void testCustomReferrerPercentText() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(.55f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fiftyFivePercentCustomReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.customReferrerPercentText.assertValues("55%");
  }

  @Test
  public void testExternalReferrerPercent() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(15f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fifteenPercentExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.externalReferrerPercent.assertValues(15f);
  }

  @Test
  public void testExternalReferrerPercentText() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(15f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(fifteenPercentExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.externalReferrerPercentText.assertValues(NumberUtils.flooredPercentage(15f * 100f));
  }

  @Test
  public void testInternalReferrerPercent() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentInternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(30f)
      .referrerType(ReferrerType.INTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(thirtyPercentInternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.internalReferrerPercent.assertValues(30f);
  }

  @Test
  public void testInternalReferrerPercentText() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentInternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(30f)
      .referrerType(ReferrerType.INTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(thirtyPercentInternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.internalReferrerPercentText.assertValues(NumberUtils.flooredPercentage(30f * 100f));
  }

  @Test
  public void testPledgedViaCustomLayoutIsGone() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats zeroPledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(0f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(zeroPledgedCustomReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.pledgedViaCustomLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaExternalLayoutIsGone() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats zeroPledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(0f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(zeroPledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.pledgedViaExternalLayoutIsGone.assertValues(true);
  }

  @Test
  public void testPledgedViaInternalLayoutIsGone() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats zeroPledgedInternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(0f)
      .referrerType(ReferrerType.INTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Collections.singletonList(zeroPledgedInternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.pledgedViaInternalLayoutIsGone.assertValues(true);
  }

  @Test
  public void testProjectAndAveragePledge() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.CumulativeStats cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats().toBuilder()
      .averagePledge(10f)
      .build();

    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().cumulative(cumulativeStats).build();
    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndAveragePledge.assertValue(Pair.create(project, 10));
  }

  @Test
  public void testProjectAndCustomReferrerPledgedAmount() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats onePledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(1f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats twoPledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(2f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats threePledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(3f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      onePledgedCustomReferrer, twoPledgedCustomReferrer, threePledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndCustomReferrerPledgedAmount.assertValue(Pair.create(project, 3f));
  }

  @Test
  public void testProjectAndExternalReferrerPledgedAmount() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats onePledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(1f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats twoPledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(2f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats threePledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(3f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      onePledgedCustomReferrer, twoPledgedCustomReferrer, threePledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndExternalReferrerPledgedAmount.assertValue(Pair.create(project, 3f));
  }

  @Test
  public void testProjectAndInternalReferrerPledgedAmount() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats onePledgedCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(1f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats twoPledgedInternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(2f)
      .referrerType(ReferrerType.INTERNAL.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats threePledgedExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .pledged(3f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      onePledgedCustomReferrer, twoPledgedInternalReferrer, threePledgedExternalReferrer);
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.projectAndInternalReferrerPledgedAmount.assertValue(Pair.create(project, 2f));
  }

  @Test
  public void testReferrerPercents() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(55f)
      .referrerType(ReferrerType.CUSTOM.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(15f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats tenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(10f)
      .referrerType(ReferrerType.EXTERNAL.getReferrerType())
      .build();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentInternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(30f)
      .referrerType(ReferrerType.INTERNAL.getReferrerType())
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      fifteenPercentExternalReferrer,
      tenPercentExternalReferrer,
      thirtyPercentInternalReferrer,
      fiftyFivePercentCustomReferrer);
    setUpEnvironment(environment());
    final ProjectStatsEnvelope statsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder().referralDistribution(referrerList).build();
    this.vm.inputs.projectAndStatsInput(Pair.create(project, statsEnvelope));
    this.customReferrerPercent.assertValues(55.0f);
    this.externalReferrerPercent.assertValues(25.0f);
    this.internalReferrerPercent.assertValues(30.0f);
  }
}
