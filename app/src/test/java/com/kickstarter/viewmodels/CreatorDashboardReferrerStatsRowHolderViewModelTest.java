package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardReferrerStatsRowHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel vm;

  private final TestSubscriber<Pair<Project, Float>> projectAndPledgedForReferrer = new TestSubscriber<>();
  private final TestSubscriber<String> referrerBackerCount = new TestSubscriber<>();
  private final TestSubscriber<Integer> referrerSourceColorId = new TestSubscriber<>();
  private final TestSubscriber<String> referrerSourceName = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectAndPledgedForReferrer().subscribe(this.projectAndPledgedForReferrer);
    this.vm.outputs.referrerBackerCount().subscribe(this.referrerBackerCount);
    this.vm.outputs.referrerSourceColorId().subscribe(this.referrerSourceColorId);
    this.vm.outputs.referrerSourceName().subscribe(this.referrerSourceName);
  }

  @Test
  public void testProjectAndPledgedForReferrer() {
    final Project project = ProjectFactory.project().toBuilder().pledged(100).build();
    final ProjectStatsEnvelope.ReferrerStats referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .pledged(50)
      .build();

    final float pledgedFloat = referrerStats.pledged();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(project, referrerStats));
    this.projectAndPledgedForReferrer.assertValue(Pair.create(project, pledgedFloat));
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
  public void testReferrerSourceColor_WhenCustom() {
    final ProjectStatsEnvelope.ReferrerStats referrerStats = getReferrerStat("custom");

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(ProjectFactory.project(), referrerStats));
    this.referrerSourceColorId.assertValues(R.color.ksr_highlighter_green);
  }

  @Test
  public void testReferrerSourceColor_WhenExternal() {
    final ProjectStatsEnvelope.ReferrerStats referrerStats = getReferrerStat("external");

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(ProjectFactory.project(), referrerStats));
    this.referrerSourceColorId.assertValues(R.color.ksr_green_500);
  }

  @Test
  public void testReferrerSourceColor_WhenKickstarter() {
    final ProjectStatsEnvelope.ReferrerStats referrerStats = getReferrerStat("kickstarter");

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(ProjectFactory.project(), referrerStats));
    this.referrerSourceColorId.assertValues(R.color.ksr_green_800);
  }

  @Test
  public void testReferrerSourceName() {
    final ProjectStatsEnvelope.ReferrerStats referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .referrerName("Friends Backed Email")
      .build();

    setUpEnvironment(environment());
    this.vm.inputs.projectAndReferrerStatsInput(Pair.create(ProjectFactory.project(), referrerStats));
    this.referrerSourceName.assertValues("Friends Backed Email");
  }

  private ProjectStatsEnvelope.ReferrerStats getReferrerStat(final @NonNull String referrerType) {
    return ProjectStatsEnvelopeFactory.ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .referrerType(referrerType)
      .build();
  }
}
