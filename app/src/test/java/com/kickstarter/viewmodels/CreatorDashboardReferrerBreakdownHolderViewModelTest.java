package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ReferrerType;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerBreakdownHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel vm;

  private final TestSubscriber<Double> customReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<Double> externalReferrerPercent = new TestSubscriber<>();
  private final TestSubscriber<Double> internalReferrerPercent = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment);
    this.vm.outputs.customReferrerPercent().subscribe(this.customReferrerPercent);
    this.vm.outputs.externalReferrerPercent().subscribe(this.externalReferrerPercent);
    this.vm.outputs.internalReferrerPercent().subscribe(this.internalReferrerPercent);
  }

  @Test
  public void testReferrerPercents() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope.ReferrerStats fiftyFivePercentCustomReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(55.0)
      .referrerType(ReferrerType.CUSTOM)
      .build();
    final ProjectStatsEnvelope.ReferrerStats fifteenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(15.0)
      .referrerType(ReferrerType.EXTERNAL)
      .build();
    final ProjectStatsEnvelope.ReferrerStats tenPercentExternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(10.0)
      .referrerType(ReferrerType.EXTERNAL)
      .build();
    final ProjectStatsEnvelope.ReferrerStats thirtyPercentInternalReferrer = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder()
      .percentageOfDollars(30.0)
      .referrerType(ReferrerType.INTERNAL)
      .build();
    final List<ProjectStatsEnvelope.ReferrerStats> referrerList = Arrays.asList(
      fifteenPercentExternalReferrer,
      tenPercentExternalReferrer,
      thirtyPercentInternalReferrer,
      fiftyFivePercentCustomReferrer);
    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, referrerList));
    this.customReferrerPercent.assertValues(55.0);
    this.externalReferrerPercent.assertValues(25.0);
    this.internalReferrerPercent.assertValues(30.0);
  }
}
