package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.ProjectsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class CreatorDashboardViewModelTest extends KSRobolectricTestCase {
  private CreatorDashboardViewModel.ViewModel vm;
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<Pair<Project, RefTag>>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CreatorDashboardViewModel.ViewModel(environment);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
  }
  @Test
  public void testProjectNameTextViewText() {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.project()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull
      Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.projectNameTextViewText.assertValues(ListUtils.first(projects).name());
  }

  @Test
  public void testStartProjectActivity() {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.project()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull
      Observable<ProjectsEnvelope> fetchProjects(final boolean member) {
        return Observable.just(ProjectsEnvelopeFactory.projectsEnvelope(projects));
      }
    };
    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.inputs.projectViewClicked();
    this.startProjectActivity.assertValues(Pair.create(ListUtils.first(projects), RefTag.dashboard()));
  }
}
