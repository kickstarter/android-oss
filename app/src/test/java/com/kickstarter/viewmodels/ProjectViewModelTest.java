package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class ProjectViewModelTest extends KSRobolectricTestCase {
  private ProjectViewModel.ViewModel vm;
  private final TestSubscriber<Integer> heartDrawableId = new TestSubscriber<>();
  private final TestSubscriber<Project> projectTest = new TestSubscriber<>();
  private final TestSubscriber<Project> showShareSheet = new TestSubscriber<>();
  private final TestSubscriber<Void> showSavedPromptTest = new TestSubscriber<>();
  private final TestSubscriber<Void> startLoginToutActivity = new TestSubscriber<>();
  private final TestSubscriber<Boolean> savedTest = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, User>> startBackingActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startCampaignWebViewActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startCommentsActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startCreatorBioWebViewActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startManagePledgeActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startProjectUpdatesActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startVideoActivity = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProjectViewModel.ViewModel(environment);
    this.vm.outputs.heartDrawableId().subscribe(this.heartDrawableId);
    this.vm.outputs.projectAndUserCountry().map(pc -> pc.first).subscribe(this.projectTest);
    this.vm.outputs.showShareSheet().subscribe(this.showShareSheet);
    this.vm.outputs.showSavedPrompt().subscribe(this.showSavedPromptTest);
    this.vm.outputs.startLoginToutActivity().subscribe(this.startLoginToutActivity);
    this.vm.outputs.projectAndUserCountry().map(pc -> pc.first.isStarred()).subscribe(this.savedTest);
    this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity);
    this.vm.outputs.startCampaignWebViewActivity().subscribe(this.startCampaignWebViewActivity);
    this.vm.outputs.startCommentsActivity().subscribe(this.startCommentsActivity);
    this.vm.outputs.startCreatorBioWebViewActivity().subscribe(this.startCreatorBioWebViewActivity);
    this.vm.outputs.startManagePledgeActivity().subscribe(this.startManagePledgeActivity);
    this.vm.outputs.startProjectUpdatesActivity().subscribe(this.startProjectUpdatesActivity);
    this.vm.outputs.startVideoActivity().subscribe(this.startVideoActivity);
  }

  @Test
  public void testProjectViewModel_EmitsProjectWithStandardSetUp() {
    final Project project = ProjectFactory.project();
    final CurrentConfigType currentConfig = new MockCurrentConfig();
    currentConfig.config(ConfigFactory.config());

    setUpEnvironment(environment().toBuilder().currentConfig(currentConfig).build());
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.projectTest.assertValues(project, project);
    this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE);
  }

  @Test
  public void testProjectViewModel_LoggedOutStarProjectFlow() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();
    environment.currentConfig().config(ConfigFactory.config());

    setUpEnvironment(environment);

    // Start the view model with a project
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.halfWayProject()));

    this.savedTest.assertValues(false, false);
    this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline);

    // Try starring while logged out
    this.vm.inputs.heartButtonClicked();

    // The project shouldn't be saved, and a login prompt should be shown.
    this.savedTest.assertValues(false, false);
    this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline);
    this.showSavedPromptTest.assertValueCount(0);
    this.startLoginToutActivity.assertValueCount(1);

    // A koala event for starring should NOT be tracked
    this.koalaTest.assertValues(KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE);

    // Login
    currentUser.refresh(UserFactory.user());

    // The project should be saved, and a star prompt should be shown.
    this.savedTest.assertValues(false, false, true);
    this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart);
    this.showSavedPromptTest.assertValueCount(1);

    // A koala event for starring should be tracked
    this.koalaTest.assertValues(
      KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE, KoalaEvent.PROJECT_STAR, KoalaEvent.STARRED_PROJECT
    );
  }

  @Test
  public void testProjectViewModel_ShowShareSheet() {
    final Project project = ProjectFactory.project();
    final User user = UserFactory.user();

    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(user)).build());
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.inputs.shareButtonClicked();
    this.showShareSheet.assertValues(project);
    this.koalaTest.assertValues(
      KoalaEvent.PROJECT_PAGE, KoalaEvent.VIEWED_PROJECT_PAGE,
      KoalaEvent.PROJECT_SHOW_SHARE_SHEET_LEGACY, KoalaEvent.SHOWED_SHARE_SHEET
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

    setUpEnvironment(environment);

    // Start the view model with an almost completed project
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Login
    currentUser.refresh(UserFactory.user());

    // Star the project
    this.vm.inputs.heartButtonClicked();

    // The project should be saved, and a save prompt should NOT be shown.
    this.savedTest.assertValues(false, false, true);
    this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart);
    this.showSavedPromptTest.assertValueCount(0);
  }

  @Test
  public void testProjectViewModel_SaveProjectThatIsSuccessful() {
    final CurrentUserType currentUser = new MockCurrentUser();
    final Environment environment = environment().toBuilder()
      .currentUser(currentUser)
      .build();
    environment.currentConfig().config(ConfigFactory.config());

    setUpEnvironment(environment);

    // Start the view model with a successful project
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.successfulProject()));

    // Login
    currentUser.refresh(UserFactory.user());

    // Star the project
    this.vm.inputs.heartButtonClicked();

    // The project should be saved, and a save prompt should NOT be shown.
    this.savedTest.assertValues(false, false, true);
    this.heartDrawableId.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart_outline, R.drawable.icon__heart);
    this.showSavedPromptTest.assertValueCount(0);
  }

  @Test
  public void testProjectViewModel_StartBackingActivity() {
    final Project project = ProjectFactory.project();
    final User user = UserFactory.user();

    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(user)).build());
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.inputs.viewPledgeButtonClicked();
    this.startBackingActivity.assertValues(Pair.create(project, user));
  }

  @Test
  public void testProjectViewModel_StartCampaignWebViewActivity() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Start the view model with a project.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.inputs.blurbTextViewClicked();
    this.startCampaignWebViewActivity.assertValues(project);
  }

  @Test
  public void testProjectViewModel_StartCreatorBioWebViewActivity() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Start the view model with a project.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.inputs.creatorNameTextViewClicked();
    this.startCreatorBioWebViewActivity.assertValues(project);
  }

  @Test
  public void testProjectViewModel_StartCommentsActivity() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Start the view model with a project.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.inputs.commentsTextViewClicked();
    this.startCommentsActivity.assertValues(project);
  }

  @Test
  public void testProjectViewModel_StartManagePledgeActivity() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Start the view model with a project.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Click on Manage pledge button.
    this.vm.inputs.managePledgeButtonClicked();
    this.startManagePledgeActivity.assertValues(project);
  }

  @Test
  public void testProjectViewModel_StartProjectUpdatesActivity() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Start the view model with a project.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    // Click on Updates button.
    this.vm.inputs.updatesTextViewClicked();
    this.startProjectUpdatesActivity.assertValues(project);
  }

  @Test
  public void testProjectViewModel_StartVideoActivity() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Start the view model with a project.
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.vm.inputs.playVideoButtonClicked();
    this.startVideoActivity.assertValues(project);
  }
}
