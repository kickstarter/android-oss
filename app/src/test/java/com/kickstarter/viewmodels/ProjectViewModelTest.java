package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class ProjectViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProjectViewModel_EmitsProjectWithStandardSetUp() {
    final ProjectViewModel vm = new ProjectViewModel(environment());

    final TestSubscriber<Project> projectTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first).subscribe(projectTest);

    final Project project = ProjectFactory.project();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    projectTest.assertValues(project, project);

    koalaTest.assertValues("Project Page");
  }

  @Test
  public void testProjectViewModel_LoggedOutStarProjectFlow() {
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final ProjectViewModel vm = new ProjectViewModel(environment);

    final TestSubscriber<Void> loginToutTest = new TestSubscriber<>();
    vm.outputs.showLoginTout().subscribe(loginToutTest);

    final TestSubscriber<Void> showStarredPromptTest = new TestSubscriber<>();
    vm.outputs.showStarredPrompt().subscribe(showStarredPromptTest);

    final TestSubscriber<Boolean> starredTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first).map(Project::isStarred).subscribe(starredTest);

    // Start the view model with a project
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.halfWayProject()));

    starredTest.assertValues(false, false);

    // Try starring while logged out
    vm.inputs.starClicked();

    // The project shouldn't be starred, and a login prompt should be shown.
    starredTest.assertValues(false, false);
    showStarredPromptTest.assertValueCount(0);
    loginToutTest.assertValueCount(1);

    // A koala event for starring should NOT be tracked
    koalaTest.assertValues("Project Page");

    // Login
    currentUser.refresh(UserFactory.user());
    vm.inputs.loginSuccess();

    // The project should be starred, and a star prompt should be shown.
    starredTest.assertValues(false, false, true);
    showStarredPromptTest.assertValueCount(1);

    // A koala event for starring should be tracked
    koalaTest.assertValues("Project Page", "Project Star");
  }

  @Test
  public void testProjectViewModel_StarProjectThatIsAlmostCompleted() {
    final Project project = ProjectFactory.almostCompletedProject();

    final CurrentUserType currentUser = new MockCurrentUser();
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final ProjectViewModel vm = new ProjectViewModel(environment);

    final TestSubscriber<Void> showStarredPromptTest = new TestSubscriber<>();
    vm.outputs.showStarredPrompt().subscribe(showStarredPromptTest);

    final TestSubscriber<Boolean> starredTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first).map(Project::isStarred).subscribe(starredTest);

    // Start the view model with an almost completed project
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Login
    currentUser.refresh(UserFactory.user());

    // Star the project
    vm.inputs.starClicked();

    // The project should be starred, and a star prompt should NOT be shown.
    starredTest.assertValues(false, false, true);
    showStarredPromptTest.assertValueCount(0);
  }

  @Test
  public void testProjectViewModel_StarProjectThatIsSuccessful() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();

    final ProjectViewModel vm = new ProjectViewModel(environment);

    final TestSubscriber<Void> showStarredPromptTest = new TestSubscriber<>();
    vm.outputs.showStarredPrompt().subscribe(showStarredPromptTest);

    final TestSubscriber<Boolean> starredTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first).map(Project::isStarred).subscribe(starredTest);

    // Start the view model with a successful project
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()));

    // Login
    currentUser.refresh(UserFactory.user());

    // Star the project
    vm.inputs.starClicked();

    // The project should be starred, and a star prompt should NOT be shown.
    starredTest.assertValues(false, false, true);
    showStarredPromptTest.assertValueCount(0);
  }
}
