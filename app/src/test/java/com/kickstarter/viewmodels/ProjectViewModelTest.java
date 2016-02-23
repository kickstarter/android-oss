package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class ProjectViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProjectViewModel_EmitsProjectWithStandardSetUp() {
    final ProjectViewModel vm = new ProjectViewModel(environment());

    final TestSubscriber<Project> projectTest = new TestSubscriber<>();
    vm.outputs.projectAndConfig().map(pc -> pc.first).subscribe(projectTest);

    final Project project = ProjectFactory.halfWayProject();
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.PROJECT, project);
    vm.intent(intent);

    projectTest.assertValues(project, project);

    koalaTest.assertValues("Project Page");
  }

  @Test
  public void testProjectViewModel_StarClickWhileLoggedOut() {
    final ProjectViewModel vm = new ProjectViewModel(environment());

    final TestSubscriber<Void> loginToutTest = new TestSubscriber<>();
    vm.outputs.showLoginTout().subscribe(loginToutTest);

    final Project project = ProjectFactory.halfWayProject();
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.PROJECT, project);
    vm.intent(intent);

    vm.inputs.starClicked();

    loginToutTest.assertValueCount(1);

    vm.inputs.loginSuccess();
  }
}
