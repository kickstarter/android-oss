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
  private ProfileViewModel.ViewModel vm;

  private final TestSubscriber<String> avatarImageViewUrl = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backedCountTextViewHidden = new TestSubscriber<>();
  private final TestSubscriber<String> backedCountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backedTextViewHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> createdCountTextViewHidden = new TestSubscriber<>();
  private final TestSubscriber<String> createdCountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> createdTextViewHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> dividerViewHidden = new TestSubscriber<>();
  private final TestSubscriber<List<Project>> projects = new TestSubscriber<>();
  private final TestSubscriber<Void> resumeDiscoveryActivity = new TestSubscriber<>();
  private final TestSubscriber<Void> startMessageThreadsActivity = new TestSubscriber<>();
  private final TestSubscriber<Project> startProjectActivity = new TestSubscriber<>();
  private final TestSubscriber<String> userNameTextViewText = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProfileViewModel.ViewModel(environment);

    this.vm.outputs.avatarImageViewUrl().subscribe(this.avatarImageViewUrl);
    this.vm.outputs.backedCountTextViewHidden().subscribe(this.backedCountTextViewHidden);
    this.vm.outputs.backedCountTextViewText().subscribe(this.backedCountTextViewText);
    this.vm.outputs.backedTextViewHidden().subscribe(this.backedTextViewHidden);
    this.vm.outputs.createdCountTextViewHidden().subscribe(this.createdCountTextViewHidden);
    this.vm.outputs.createdCountTextViewText().subscribe(this.createdCountTextViewText);
    this.vm.outputs.createdTextViewHidden().subscribe(this.createdTextViewHidden);
    this.vm.outputs.dividerViewHidden().subscribe(this.dividerViewHidden);
    this.vm.outputs.projects().subscribe(this.projects);
    this.vm.outputs.resumeDiscoveryActivity().subscribe(this.resumeDiscoveryActivity);
    this.vm.outputs.startMessageThreadsActivity().subscribe(this.startMessageThreadsActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.userNameTextViewText().subscribe(this.userNameTextViewText);
  }

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

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    // Backed text views are displayed.
    this.backedCountTextViewHidden.assertValues(false);
    this.backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()));
    this.backedTextViewHidden.assertValues(false);

    // Created text views are displayed.
    this.createdCountTextViewHidden.assertValues(false);
    this.createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()));
    this.createdTextViewHidden.assertValues(false);

    // Divider view is displayed.
    this.dividerViewHidden.assertValues(false);
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

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    // Backed text views are displayed.
    this.backedCountTextViewHidden.assertValues(false);
    this.backedCountTextViewText.assertValues(NumberUtils.format(user.backedProjectsCount()));
    this.backedTextViewHidden.assertValues(false);

    // Created text views are hidden.
    this.createdCountTextViewHidden.assertValues(true);
    this.createdCountTextViewText.assertNoValues();
    this.createdTextViewHidden.assertValues(true);

    // Divider view is hidden.
    this.dividerViewHidden.assertValues(true);
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

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    // Backed text views are hidden.
    this.backedCountTextViewHidden.assertValues(true);
    this.backedCountTextViewText.assertNoValues();
    this.backedTextViewHidden.assertValues(true);

    // Created text views are displayed.
    this.createdCountTextViewHidden.assertValues(false);
    this.createdCountTextViewText.assertValues(NumberUtils.format(user.createdProjectsCount()));
    this.createdTextViewHidden.assertValues(false);

    // Divider view is hidden.
    this.dividerViewHidden.assertValues(true);
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

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    this.koalaTest.assertValues(KoalaEvent.PROFILE_VIEW_MY, KoalaEvent.VIEWED_PROFILE);
    this.projects.assertValueCount(1);
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

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    this.avatarImageViewUrl.assertValues(user.avatar().medium());
    this.userNameTextViewText.assertValues(user.name());
  }

  @Test
  public void testProfileViewModel_ResumeDiscoveryActivity() {
    setUpEnvironment(environment());

    this.vm.inputs.exploreProjectsButtonClicked();
    this.resumeDiscoveryActivity.assertValueCount(1);
  }

  @Test
  public void testProfileViewModel_StartMessageThreadsActivity() {
    setUpEnvironment(environment());

    this.vm.inputs.messsagesButtonClicked();
    this.startMessageThreadsActivity.assertValueCount(1);
  }

  @Test
  public void testProfileViewModel_StartProjectActivity() {
    setUpEnvironment(environment());

    this.vm.inputs.projectCardClicked(ProjectFactory.project());
    this.startProjectActivity.assertValueCount(1);
  }
}
