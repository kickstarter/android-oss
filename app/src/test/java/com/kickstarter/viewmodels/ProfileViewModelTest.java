package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.DiscoverEnvelopeFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class ProfileViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProfileViewModel_EmitsProjects() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(
          DiscoverEnvelopeFactory.discoverEnvelope(Collections.singletonList(ProjectFactory.project()))
        );
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();

    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(env);

    final TestSubscriber<List<Project>> projects = new TestSubscriber<>();
    vm.outputs.projects().subscribe(projects);

    koalaTest.assertValues(KoalaEvent.PROFILE_VIEW_MY, KoalaEvent.VIEWED_PROFILE);
    projects.assertValueCount(1);
  }

  @Test
  public void testProfileViewModel_ResumeDiscoveryActivity() {
    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(environment());

    final TestSubscriber<Void> resumeDiscoveryActivity = new TestSubscriber<>();
    vm.outputs.resumeDiscoveryActivity().subscribe(resumeDiscoveryActivity);

    vm.inputs.exploreProjectsButtonClicked();
    resumeDiscoveryActivity.assertValueCount(1);
  }

  @Test
  public void testProfileViewModel_StartProjectActivity() {
    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(environment());
    final Project project = ProjectFactory.backedProject();

    final TestSubscriber<Project> startProjectActivity = new TestSubscriber<>();
    vm.outputs.startProjectActivity().subscribe(startProjectActivity);

    vm.inputs.projectCardClicked(project);
    startProjectActivity.assertValues(project);
  }
}
