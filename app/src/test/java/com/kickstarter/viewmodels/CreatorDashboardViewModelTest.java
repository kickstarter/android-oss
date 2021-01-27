package com.kickstarter.viewmodels;


import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.mock.factories.ProjectsEnvelopeFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.data.ProjectDashboardData;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class CreatorDashboardViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardViewModel.ViewModel vm;

  private final TestSubscriber<Boolean> bottomSheetShouldExpand = new TestSubscriber<>();
  private final TestSubscriber<ProjectDashboardData> projectDashboardData = new TestSubscriber<>();
  private final TestSubscriber<List<Project>> projectsForBottomSheet = new TestSubscriber<>();
  private final TestSubscriber<String> projectName = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardViewModel.ViewModel(environment);
    this.vm.outputs.bottomSheetShouldExpand().subscribe(this.bottomSheetShouldExpand);
    this.vm.outputs.projectDashboardData().subscribe(this.projectDashboardData);
    this.vm.outputs.projectsForBottomSheet().subscribe(this.projectsForBottomSheet);
    this.vm.outputs.projectName().subscribe(this.projectName);
  }

  @Test
  public void testBottomSheetShouldExpand_whenBackClicked() {
    setUpEnvironment(environment());
    this.vm.intent(new Intent());
    this.vm.inputs.backClicked();

    this.bottomSheetShouldExpand.assertValue(false);
  }

  @Test
  public void testBottomSheetShouldExpand_whenNewProjectSelected() {
    setUpEnvironment(environment());
    this.vm.intent(new Intent());
    this.vm.inputs.projectSelectionInput(ProjectFactory.project());

    this.bottomSheetShouldExpand.assertValue(false);
  }

  @Test
  public void testBottomSheetShouldExpand_whenProjectsListButtonClicked() {
    setUpEnvironment(environment());
    this.vm.intent(new Intent());
    this.vm.inputs.projectsListButtonClicked();

    this.bottomSheetShouldExpand.assertValue(true);
  }

  @Test
  public void testBottomSheetShouldExpand_whenScrimClicked() {
    setUpEnvironment(environment());
    this.vm.intent(new Intent());
    this.vm.inputs.scrimClicked();

    this.bottomSheetShouldExpand.assertValue(false);
  }

  public void testProjectDashboardData_whenViewingAllProjects() {
    final List<Project> projects = Collections.singletonList(ProjectFactory.project());

    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();
    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
      @Override public @NonNull
      Observable<ProjectStatsEnvelope> fetchProjectStats(final @NonNull Project project) {
        return Observable.just(projectStatsEnvelope);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent());
    this.projectDashboardData.assertValue(new ProjectDashboardData(Objects.requireNonNull(ListUtils.first(projects)), projectStatsEnvelope, false));
  }

  public void testProjectDashboardData_whenViewingSingleProjects() {
    final Project project = ProjectFactory.project();

    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();
    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<ProjectStatsEnvelope> fetchProjectStats(final @NonNull Project project) {
        return Observable.just(projectStatsEnvelope);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.projectDashboardData.assertValue(new ProjectDashboardData(project, projectStatsEnvelope, true));
  }

  @Test
  public void testProjectsForBottomSheet_With1Project() {
    final List<Project> projects = Collections.singletonList(ProjectFactory.project());
    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull
      Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
    };
    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.projectsForBottomSheet.assertNoValues();
  }

  @Test
  public void testProjectsForBottomSheet_WithManyProjects() {
    final Project project1 = ProjectFactory.project();
    final Project project2 = ProjectFactory.project();
    final List<Project> projects = Arrays.asList(
      project1,
      project2
    );
    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull
      Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
    };
    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent());
    this.projectsForBottomSheet.assertValue(Collections.singletonList(project2));
  }

  @Test
  public void testProjectSwitcherProjectClickOutput() {
    DateTimeUtils.setCurrentMillisFixed(new DateTime().getMillis());

    final Project project1 = ProjectFactory.project();
    final Project project2 = ProjectFactory.project();
    final List<Project> projects = Arrays.asList(
      project1,
      project2
    );

    final ProjectStatsEnvelope projectStatsEnvelope = ProjectStatsEnvelopeFactory.projectStatsEnvelope();
    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
      @Override public @NonNull
      Observable<ProjectStatsEnvelope> fetchProjectStats(final @NonNull Project project) {
        return Observable.just(projectStatsEnvelope);
      }
    };
    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent());
    this.vm.inputs.projectSelectionInput(project2);
    this.projectDashboardData.assertValues(new ProjectDashboardData(project1, ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false),
      new ProjectDashboardData(project2, ProjectStatsEnvelopeFactory.projectStatsEnvelope(), false));
  }

  @Test
  public void testProjectName_whenMultipleProjects() {
    final Project project1 = ProjectFactory.project()
      .toBuilder()
      .name("Best Project 2K19")
      .build();
    final Project project2 = ProjectFactory.project();
    final List<Project> projects = Arrays.asList(
      project1,
      project2
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
    };
    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent());
    this.projectName.assertValue("Best Project 2K19");
  }

  @Test
  public void testProjectName_whenSingleProject() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .name("Best Project 2K19")
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(Collections.singletonList(project)));
      }
    };
    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    this.projectName.assertValue("Best Project 2K19");
  }
}
