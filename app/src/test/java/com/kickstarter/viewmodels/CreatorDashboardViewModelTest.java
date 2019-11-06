package com.kickstarter.viewmodels;


import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory;
import com.kickstarter.mock.factories.ProjectsEnvelopeFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class CreatorDashboardViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardViewModel.ViewModel vm;

  private final TestSubscriber<Void> openBottomSheet = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, ProjectStatsEnvelope>> projectAndStats = new TestSubscriber<>();
  private final TestSubscriber<List<Project>> projectsForBottomSheet = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardViewModel.ViewModel(environment);
    this.vm.outputs.openBottomSheet().subscribe(this.openBottomSheet);
    this.vm.outputs.projectDashboardData().subscribe(this.projectAndStats);
    this.vm.outputs.projectsForBottomSheet().subscribe(this.projectsForBottomSheet);
  }

  public void testProjectAndStats() {
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
    final Pair<Project, ProjectStatsEnvelope> outputPair = Pair.create(ListUtils.first(projects), projectStatsEnvelope);
    this.projectAndStats.assertValue(outputPair);
    this.koalaTest.assertValues(KoalaEvent.VIEWED_PROJECT_DASHBOARD);
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
    this.projectsForBottomSheet.assertValue(Collections.singletonList(project2));
  }

  @Test
  public void testProjectSwitcherProjectClickOutput() {
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

    this.vm.inputs.projectSelectionInput(project2);
    this.projectAndStats.assertValues(Pair.create(project1, ProjectStatsEnvelopeFactory.projectStatsEnvelope()), Pair.create(project2, ProjectStatsEnvelopeFactory.projectStatsEnvelope()));
    this.koalaTest.assertValues(KoalaEvent.VIEWED_PROJECT_DASHBOARD, KoalaEvent.SWITCHED_PROJECTS, KoalaEvent.VIEWED_PROJECT_DASHBOARD);
  }

  @Test
  public void testProjectsListButtonClicked() {
    setUpEnvironment(environment());
    this.vm.inputs.projectsListButtonClicked();
    this.openBottomSheet.assertValueCount(1);
    this.koalaTest.assertValue(KoalaEvent.OPENED_PROJECT_SWITCHER);
  }
}
