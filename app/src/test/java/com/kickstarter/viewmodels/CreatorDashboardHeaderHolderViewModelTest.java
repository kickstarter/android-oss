package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.data.ProjectDashboardData;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public class CreatorDashboardHeaderHolderViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardHeaderHolderViewModel.ViewModel vm;

  private final TestSubscriber<Boolean> messagesButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> otherProjectsButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> percentageFunded = new TestSubscriber<>();
  private final TestSubscriber<Integer> percentageFundedProgress = new TestSubscriber<>();
  private final TestSubscriber<String> projectBackersCountText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Integer> progressBarBackground = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startMessageThreadsActivity = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<>();
  private final TestSubscriber<Boolean> viewProjectButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> timeRemainingText = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment);
    this.vm.outputs.messagesButtonIsGone().subscribe(this.messagesButtonIsGone);
    this.vm.outputs.otherProjectsButtonIsGone().subscribe(this.otherProjectsButtonIsGone);
    this.vm.outputs.projectBackersCountText().subscribe(this.projectBackersCountText);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.percentageFunded().subscribe(this.percentageFunded);
    this.vm.outputs.percentageFundedProgress().subscribe(this.percentageFundedProgress);
    this.vm.outputs.progressBarBackground().subscribe(this.progressBarBackground);
    this.vm.outputs.startMessageThreadsActivity().subscribe(this.startMessageThreadsActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.timeRemainingText().subscribe(this.timeRemainingText);
    this.vm.outputs.viewProjectButtonIsGone().subscribe(this.viewProjectButtonIsGone);
  }

  @Test
  public void testMessagesButtonIsGone_whenCurrentUserIsCollaborator() {
    final User creator = UserFactory.creator();
    final CurrentUserType currentUser = new MockCurrentUser(UserFactory.collaborator());

    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());
    this.vm.inputs.configureWith(new ProjectDashboardData(project, projectStatsEnvelope, false));

    // Messages button is gone if current user is not the project creator (e.g. a collaborator).
    this.messagesButtonIsGone.assertValue(true);
  }

  @Test
  public void testMessagesButtonIsGone_whenCurrentUserIsProjectCreator() {
    final User creator = UserFactory.creator();
    final CurrentUserType currentUser = new MockCurrentUser(creator);
    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.configureWith(new ProjectDashboardData(project, ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false));

    // Messages button is shown to project creator.
    this.messagesButtonIsGone.assertValues(false);
  }

  @Test
  public void testOtherProjectsButtonIsGone_whenCurrentUserIsMemberOf1Project() {
    final User collaboratorWith1Project = UserFactory.collaborator()
      .toBuilder()
      .memberProjectsCount(1)
      .build();
    final CurrentUserType collaborator = new MockCurrentUser(collaboratorWith1Project);

    setUpEnvironment(environment().toBuilder().currentUser(collaborator).build());
    this.vm.inputs.configureWith(new ProjectDashboardData(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false));

    this.otherProjectsButtonIsGone.assertValue(true);
  }

  @Test
  public void testOtherProjectsButtonIsGone_whenCurrentUserIsMemberOfManyProjects_viewingAllProjects() {
    final CurrentUserType collaborator = new MockCurrentUser(UserFactory.collaborator());

    setUpEnvironment(environment().toBuilder().currentUser(collaborator).build());
    this.vm.inputs.configureWith(new ProjectDashboardData(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false));

    this.otherProjectsButtonIsGone.assertValue(false);
  }

  @Test
  public void testOtherProjectsButtonIsGone_whenCurrentUserIsMemberOfManyProjects_viewingSingleProject() {
    final CurrentUserType collaborator = new MockCurrentUser(UserFactory.collaborator());

    setUpEnvironment(environment().toBuilder().currentUser(collaborator).build());
    this.vm.inputs.configureWith(new ProjectDashboardData(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope(), true));

    this.otherProjectsButtonIsGone.assertValue(true);
  }

  @Test
  public void testProjectBackersCountText() {
    final Project project = ProjectFactory.project().toBuilder().backersCount(10).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.configureWith(new ProjectDashboardData(project, projectStatsEnvelope, false));
    this.projectBackersCountText.assertValue("10");
  }

  @Test
  public void testProjectNameTextViewText() {
    final Project project = ProjectFactory.project().toBuilder().name("somebody once told me").build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.configureWith(new ProjectDashboardData(project, projectStatsEnvelope, false));
    this.projectNameTextViewText.assertValue("somebody once told me");
  }

  @Test
  public void testPercentageFunded() {
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    this.vm.inputs.configureWith(new ProjectDashboardData(project, projectStatsEnvelope, false));
    final String percentageFundedOutput = NumberUtils.flooredPercentage(project.percentageFunded());
    this.percentageFunded.assertValues(percentageFundedOutput);
    final int percentageFundedProgressOutput = ProgressBarUtils.progress(project.percentageFunded());
    this.percentageFundedProgress.assertValue(percentageFundedProgressOutput);
  }

  @Test
  public void testProgressBarBackground_LiveProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_LIVE));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_SubmittedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_SUBMITTED));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_StartedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_STARTED));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_SuccessfulProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_SUCCESSFUL));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_green_horizontal);
  }

  @Test
  public void testProgressBarBackground_FailedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_FAILED));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_grey_horizontal);
  }

  @Test
  public void testProgressBarBackground_CanceledProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_CANCELED));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_grey_horizontal);
  }

  @Test
  public void testProgressBarBackground_SuspendedProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(getDashboardDataForProjectState(Project.STATE_SUSPENDED));
    this.progressBarBackground.assertValue(R.drawable.progress_bar_grey_horizontal);
  }

  @Test
  public void testStartMessagesActivity() {
    final User creator = UserFactory.creator();
    final CurrentUserType currentUser = new MockCurrentUser(creator);
    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.configureWith(new ProjectDashboardData(project, ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false));
    this.vm.inputs.messagesButtonClicked();

    this.startMessageThreadsActivity.assertValue(Pair.create(project, RefTag.dashboard()));
  }

  @Test
  public void testStartProjectActivity() {
    final Project project = ProjectFactory.project();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    setUpEnvironment(environment());
    this.vm.inputs.configureWith(new ProjectDashboardData(project, projectStatsEnvelope, false));
    this.vm.inputs.projectButtonClicked();
    this.startProjectActivity.assertValue(Pair.create(project, RefTag.dashboard()));
  }

  @Test
  public void testTimeRemainingText() {
    setUpEnvironment(environment());
    DateTimeUtils.setCurrentMillisFixed(new DateTime().getMillis());
    final Project project = ProjectFactory.project().toBuilder().deadline(new DateTime().plusDays(10)).build();
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();
    final int deadlineVal = ProjectUtils.deadlineCountdownValue(project);

    this.vm.inputs.configureWith(new ProjectDashboardData(project, projectStatsEnvelope, false));
    this.timeRemainingText.assertValue(NumberUtils.format(deadlineVal));
  }

  @Test
  public void testViewProjectButtonIsGone_whenViewingSingleProject() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectDashboardData(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope(), true));
    this.viewProjectButtonIsGone.assertValue(true);
  }

  @Test
  public void testViewProjectButtonIsGone_whenViewingAllProjects() {
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(new ProjectDashboardData(ProjectFactory.project(), ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false));
    this.viewProjectButtonIsGone.assertValue(false);
  }

  private ProjectDashboardData getDashboardDataForProjectState(final @Project.State String state) {
    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();

    final Project project = ProjectFactory.project().toBuilder().state(state).build();
    return new ProjectDashboardData(project, projectStatsEnvelope, false);
  }
}
