package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ConfigFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class ProjectViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProjectViewModel_EmitsProjectWithStandardSetUp() {
    final Environment environment = environment();
    environment.currentConfig().config(ConfigFactory.config());
    final ProjectViewModel.ViewModel vm = new ProjectViewModel.ViewModel(environment);

    final TestSubscriber<Project> projectTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first).subscribe(projectTest);

    final Project project = ProjectFactory.project();
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    projectTest.assertValues(project, project);

    koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE);
  }

  @Test
  public void testProjectViewModel_LoggedOutStarProjectFlow() {
    final CurrentUserType currentUser = new MockCurrentUser();

    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();
    environment.currentConfig().config(ConfigFactory.config());

    final ProjectViewModel.ViewModel vm = new ProjectViewModel.ViewModel(environment);

    final TestSubscriber<Void> loginToutTest = new TestSubscriber<>();
    vm.outputs.startLoginToutActivity().subscribe(loginToutTest);

    final TestSubscriber<Void> showStarredPromptTest = new TestSubscriber<>();
    vm.outputs.showStarredPrompt().subscribe(showStarredPromptTest);

    final TestSubscriber<Boolean> starredTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first.isStarred()).subscribe(starredTest);

    // Start the view model with a project
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.halfWayProject()));

    starredTest.assertValues(false, false);

    // Try starring while logged out
    vm.inputs.starButtonClicked();

    // The project shouldn't be starred, and a login prompt should be shown.
    starredTest.assertValues(false, false);
    showStarredPromptTest.assertValueCount(0);
    loginToutTest.assertValueCount(1);

    // A koala event for starring should NOT be tracked
    koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE);

    // Login
    currentUser.refresh(UserFactory.user());

    // The project should be starred, and a star prompt should be shown.
    starredTest.assertValues(false, false, true);
    showStarredPromptTest.assertValueCount(1);

    // A koala event for starring should be tracked
    koalaTest.assertValues(
      KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE, KoalaEvent.PROJECT_STAR, KoalaEvent.STARRED_PROJECT
    );
  }

  @Test
  public void testProjectViewModel_StarProjectThatIsAlmostCompleted() {
    final Project project = ProjectFactory.almostCompletedProject();

    final CurrentUserType currentUser = new MockCurrentUser();
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();
    environment.currentConfig().config(ConfigFactory.config());

    final ProjectViewModel.ViewModel vm = new ProjectViewModel.ViewModel(environment);

    final TestSubscriber<Void> showStarredPromptTest = new TestSubscriber<>();
    vm.outputs.showStarredPrompt().subscribe(showStarredPromptTest);

    final TestSubscriber<Boolean> starredTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first.isStarred()).subscribe(starredTest);

    // Start the view model with an almost completed project
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Login
    currentUser.refresh(UserFactory.user());

    // Star the project
    vm.inputs.starButtonClicked();

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
    environment.currentConfig().config(ConfigFactory.config());

    final ProjectViewModel.ViewModel vm = new ProjectViewModel.ViewModel(environment);

    final TestSubscriber<Void> showStarredPromptTest = new TestSubscriber<>();
    vm.outputs.showStarredPrompt().subscribe(showStarredPromptTest);

    final TestSubscriber<Boolean> starredTest = new TestSubscriber<>();
    vm.outputs.projectAndUserCountry().map(pc -> pc.first.isStarred()).subscribe(starredTest);

    // Start the view model with a successful project
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()));

    // Login
    currentUser.refresh(UserFactory.user());

    // Star the project
    vm.inputs.starButtonClicked();

    // The project should be starred, and a star prompt should NOT be shown.
    starredTest.assertValues(false, false, true);
    showStarredPromptTest.assertValueCount(0);
  }

  @Test
  public void testProjectViewMdoel_StartProjectUpdatesActivity() {
    final ProjectViewModel.ViewModel vm = new ProjectViewModel.ViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> startProjectUpdatesActivity = new TestSubscriber<>();
    vm.outputs.startProjectUpdatesActivity().subscribe(startProjectUpdatesActivity);

    // Start the view model with a project.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Click on Updates button.
    vm.inputs.updatesTextViewClicked();
    startProjectUpdatesActivity.assertValues(project);
  }
}
