package com.kickstarter.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.ArgumentsKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardFragmentViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardFragmentViewModel.ViewModel vm;

  private TestSubscriber<Pair<Project, ProjectStatsEnvelope>> projectAndStats = new TestSubscriber<>();
  private TestSubscriber<Void> toggleBottomSheet = new TestSubscriber<>();

  public void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardFragmentViewModel.ViewModel(environment);
    this.vm.outputs.projectAndStats().subscribe(this.projectAndStats);
    this.vm.outputs.toggleBottomSheet().subscribe(this.toggleBottomSheet);
  }

  @Test
  public void testProjectAndStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStats = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    final Bundle bundle = new Bundle();
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT, project);
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT_STATS, projectStats);
    this.vm.arguments(bundle);

    this.projectAndStats.assertValue(Pair.create(project, projectStats));
  }

  @Test
  public void testToggleBottomSheet() {
    setUpEnvironment(environment());
    this.vm.inputs.projectsListButtonClicked();
    this.toggleBottomSheet.assertValueCount(1);
    this.koalaTest.assertValue(KoalaEvent.OPENED_PROJECT_SWITCHER);
  }
}
