package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.DiscoverEnvelopeFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
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
  public void testProfileViewModel_EmitsBackedAndCreatedProjectsData() {
    final User user = UserFactory.user().toBuilder()
      .backedProjectsCount(15)
      .createdProjectsCount(2)
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(env);

    final TestSubscriber<Boolean> backedCountTextViewHidden = new TestSubscriber<>();
    vm.outputs.backedCountTextViewHidden().subscribe(backedCountTextViewHidden);

    final TestSubscriber<String> backedCountTextViewText = new TestSubscriber<>();
    vm.outputs.backedCountTextViewText().subscribe(backedCountTextViewText);

    final TestSubscriber<Boolean> backedTextViewHidden = new TestSubscriber<>();
    vm.outputs.backedTextViewHidden().subscribe(backedTextViewHidden);

    final TestSubscriber<Boolean> createdCountTextViewHidden = new TestSubscriber<>();
    vm.outputs.createdCountTextViewHidden().subscribe(createdCountTextViewHidden);

    final TestSubscriber<String> createdCountTextViewText = new TestSubscriber<>();
    vm.outputs.createdCountTextViewText().subscribe(createdCountTextViewText);

    final TestSubscriber<Boolean> createdTextViewHidden = new TestSubscriber<>();
    vm.outputs.createdTextViewHidden().subscribe(createdTextViewHidden);

    final TestSubscriber<Boolean> dividerViewHidden = new TestSubscriber<>();
    vm.outputs.dividerViewHidden().subscribe(dividerViewHidden);

    // Backed text views are displayed.
    backedCountTextViewHidden.assertValues(false);
    backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()));
    backedTextViewHidden.assertValues(false);

    // Created text views are displayed.
    createdCountTextViewHidden.assertValues(false);
    createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()));
    createdTextViewHidden.assertValues(false);

    // Divider view is displayed.
    dividerViewHidden.assertValues(false);
  }

  @Test
  public void testProfileViewModel_EmitsBackedProjectsData() {
    final User user = UserFactory.user().toBuilder()
      .backedProjectsCount(5)
      .createdProjectsCount(0)
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(env);

    final TestSubscriber<Boolean> backedCountTextViewHidden = new TestSubscriber<>();
    vm.outputs.backedCountTextViewHidden().subscribe(backedCountTextViewHidden);

    final TestSubscriber<String> backedCountTextViewText = new TestSubscriber<>();
    vm.outputs.backedCountTextViewText().subscribe(backedCountTextViewText);

    final TestSubscriber<Boolean> backedTextViewHidden = new TestSubscriber<>();
    vm.outputs.backedTextViewHidden().subscribe(backedTextViewHidden);

    final TestSubscriber<Boolean> createdCountTextViewHidden = new TestSubscriber<>();
    vm.outputs.createdCountTextViewHidden().subscribe(createdCountTextViewHidden);

    final TestSubscriber<String> createdCountTextViewText = new TestSubscriber<>();
    vm.outputs.createdCountTextViewText().subscribe(createdCountTextViewText);

    final TestSubscriber<Boolean> createdTextViewHidden = new TestSubscriber<>();
    vm.outputs.createdTextViewHidden().subscribe(createdTextViewHidden);

    final TestSubscriber<Boolean> dividerViewHidden = new TestSubscriber<>();
    vm.outputs.dividerViewHidden().subscribe(dividerViewHidden);

    // Backed text views are displayed.
    backedCountTextViewHidden.assertValues(false);
    backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()));
    backedTextViewHidden.assertValues(false);

    // Created text views are hidden.
    createdCountTextViewHidden.assertValues(true);
    createdCountTextViewText.assertNoValues();
    createdTextViewHidden.assertValues(true);

    // Divider view is hidden.
    dividerViewHidden.assertValues(true);
  }

  @Test
  public void testProfileViewModel_EmitsCreatedProjectsData() {
    final User user = UserFactory.user().toBuilder()
      .backedProjectsCount(0)
      .createdProjectsCount(2)
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(env);

    final TestSubscriber<Boolean> backedCountTextViewHidden = new TestSubscriber<>();
    vm.outputs.backedCountTextViewHidden().subscribe(backedCountTextViewHidden);

    final TestSubscriber<String> backedCountTextViewText = new TestSubscriber<>();
    vm.outputs.backedCountTextViewText().subscribe(backedCountTextViewText);

    final TestSubscriber<Boolean> backedTextViewHidden = new TestSubscriber<>();
    vm.outputs.backedTextViewHidden().subscribe(backedTextViewHidden);

    final TestSubscriber<Boolean> createdCountTextViewHidden = new TestSubscriber<>();
    vm.outputs.createdCountTextViewHidden().subscribe(createdCountTextViewHidden);

    final TestSubscriber<String> createdCountTextViewText = new TestSubscriber<>();
    vm.outputs.createdCountTextViewText().subscribe(createdCountTextViewText);

    final TestSubscriber<Boolean> createdTextViewHidden = new TestSubscriber<>();
    vm.outputs.createdTextViewHidden().subscribe(createdTextViewHidden);

    final TestSubscriber<Boolean> dividerViewHidden = new TestSubscriber<>();
    vm.outputs.dividerViewHidden().subscribe(dividerViewHidden);

    // Backed text views are hidden.
    backedCountTextViewHidden.assertValues(true);
    backedCountTextViewText.assertNoValues();
    backedTextViewHidden.assertValues(true);

    // Created text views are displayed.
    createdCountTextViewHidden.assertValues(false);
    createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()));
    createdTextViewHidden.assertValues(false);

    // Divider view is hidden.
    dividerViewHidden.assertValues(true);
  }

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
  public void testProfileViewModel_EmitsUserNameAndAvatar() {
    final User user = UserFactory.user();
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final ProfileViewModel.ViewModel vm = new ProfileViewModel.ViewModel(env);

    final TestSubscriber<String> avatarImageViewUrl = new TestSubscriber<>();
    vm.outputs.avatarImageViewUrl().subscribe(avatarImageViewUrl);

    final TestSubscriber<String> userNameTextViewText = new TestSubscriber<>();
    vm.outputs.userNameTextViewText().subscribe(userNameTextViewText);

    avatarImageViewUrl.assertValues(user.avatar().medium());
    userNameTextViewText.assertValues(user.name());
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
