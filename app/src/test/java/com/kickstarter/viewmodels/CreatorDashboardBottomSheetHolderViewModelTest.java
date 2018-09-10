package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public class CreatorDashboardBottomSheetHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardBottomSheetHolderViewModel.ViewModel vm;

  private final TestSubscriber<String> projectName = new TestSubscriber<>();
  private final TestSubscriber<DateTime> projectLaunchDate = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardBottomSheetHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectNameText().subscribe(this.projectName);
    this.vm.outputs.projectLaunchDate().subscribe(this.projectLaunchDate);
  }

  @Test
  public void testProjectNameText() {
    setUpEnvironment(environment());

    final String projectName = "Test Project";
    final DateTime now = DateTime.now();
    final Project project = ProjectFactory.project().toBuilder().name(projectName).launchedAt(now).build();

    this.vm.inputs.projectInput(project);
    this.projectName.assertValues(projectName);
    this.projectLaunchDate.assertValue(now);
  }
}
