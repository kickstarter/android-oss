package com.kickstarter.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.Environment;
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
    this.vm = new CreatorDashboardFragmentViewModel.ViewModel(environment());
    this.vm.outputs.projectAndStats().subscribe(this.projectAndStats);
    this.vm.outputs.toggleBottomSheet().subscribe(this.toggleBottomSheet);

  }

  @Test
  public void testProjectAndStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStats = ProjectStatsEnvelopeFactory.ProjectStatsEnvelope();

    setUpEnvironment(environment());
    final Bundle bundle = new Bundle();
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT, project);
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT_STATS, projectStats);
    this.vm.arguments(bundle);

    this.projectAndStats.assertValues(Pair.create(project, projectStats));
  }

  @Test
  public void testToggleBottomSheet() {
    setUpEnvironment(environment());
    this.vm.inputs.projectsMenuClicked();
    this.toggleBottomSheet.assertValueCount(1);
  }
}
