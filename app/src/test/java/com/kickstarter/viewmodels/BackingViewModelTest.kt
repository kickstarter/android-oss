package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.mock.factories.BackingFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApolloClient;
import com.kickstarter.models.Backing;
import com.kickstarter.models.BackingWrapper;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import androidx.annotation.NonNull;
import rx.Observable;

public final class BackingViewModelTest extends KSRobolectricTestCase {
  private BackingViewModel.ViewModel vm;

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new BackingViewModel.ViewModel(environment);
  }

  @Test
  public void testShowBackingFragmentFacing() {

    final User creatorUser = UserFactory.user()
            .toBuilder()
            .name("Kawhi Leonard")
            .build();

    final User backerUser = UserFactory.user()
            .toBuilder()
            .name("random backer")
            .build();

    final Backing backing = BackingFactory.backing(backerUser);

    final Project project = ProjectFactory.project();
    final BackingWrapper wrapper = new BackingWrapper(backing, backerUser, project);

    setUpEnvironment(envWithBacking(backing)
      .toBuilder()
      .currentUser(new MockCurrentUser(creatorUser))
      .build());

    this.vm.outputs.showBackingFragment().subscribe(it -> {
      assertNotNull(it);
      assertEquals(backing, it);
    });
  }

  /**
   * Returns an environment with a backing and logged in user.
   */
  private @NonNull Environment envWithBacking(final @NonNull Backing backing) {
    return environment().toBuilder()
      .apolloClient(
        new MockApolloClient() {
          @NotNull
          @Override
          public Observable<Backing> getBacking(final @NotNull String backingId) {
            return Observable.just(backing);
          }
        }
      )
      .currentUser(new MockCurrentUser(UserFactory.user()))
      .build();
  }
}
