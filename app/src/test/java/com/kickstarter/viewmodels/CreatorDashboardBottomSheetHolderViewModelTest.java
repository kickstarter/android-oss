package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardBottomSheetHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardBottomSheetHolderViewModel.ViewModel vm;

  private final TestSubscriber<String> projectName = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardBottomSheetHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectNameText().subscribe(this.projectName);
  }

  @Test
  public void testProjectNameText() {
    final Project project = ProjectFactory.project().toBuilder().name("somebody once told me").build();
    setUpEnvironment(environment());

    this.vm.inputs.projectInput(project);
    this.projectName.assertValues(project.name());
  }
}
