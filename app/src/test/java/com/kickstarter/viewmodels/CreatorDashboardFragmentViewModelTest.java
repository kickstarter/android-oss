package com.kickstarter.viewmodels;

import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.libs.FragmentViewModelManager;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.ArgumentsKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardFragmentViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardFragmentViewModel.ViewModel vm;

  private TestSubscriber<Pair<Project, ProjectStatsEnvelope>> projectAndStats = new TestSubscriber<>();
  private TestSubscriber<Void> toggleBottomSheet = new TestSubscriber<>();

  @Test
  public void testProjectAndStats() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStats = ProjectStatsEnvelopeFactory.ProjectStatsEnvelope();

    final Bundle bundle = new Bundle();
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT, project);
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT_STATS, projectStats);
    final FragmentViewModelManager fragManager = FragmentViewModelManager.getInstance();
    this.vm = fragManager.fetch(context(), CreatorDashboardFragmentViewModel.ViewModel.class, bundle);
    this.vm.outputs.projectAndStats().subscribe(this.projectAndStats);
    bundle.putString(FragmentViewModelManager.VIEW_MODEL_ID_KEY, "test_vm_id");
    fragManager.viewModels.put("test_vm_id", this.vm);
    this.vm.arguments(bundle);

    this.projectAndStats.assertValues(Pair.create(project, projectStats));
  }

  @Test
  public void testToggleBottomSheet() {
    this.vm = new CreatorDashboardFragmentViewModel.ViewModel(environment());
    this.vm.outputs.toggleBottomSheet().subscribe(this.toggleBottomSheet);

    /// do we want to build up a viewholder and a viewmodel just to simulate a click?
    this.vm.inputs.projectsMenuClicked();
    this.toggleBottomSheet.assertValueCount(1);
  }
}
