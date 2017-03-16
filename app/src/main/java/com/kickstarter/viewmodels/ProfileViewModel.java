package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ProfileActivity;
import com.kickstarter.ui.adapters.ProfileAdapter;
import com.kickstarter.ui.viewholders.EmptyProfileViewHolder;
import com.kickstarter.ui.viewholders.ProfileCardViewHolder;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface ProfileViewModel {

  interface Inputs {
    /** Call when the next page has been invoked. */
    void nextPage();
  }

  interface Outputs {
    /** Emits a list of projects to display in the profile. */
    Observable<List<Project>> projects();

    /** Emits the user to display in the profile. */
    Observable<User> user();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Project> startProjectActivity();

    /** Emits when we should resume the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<Void> resumeDiscoveryActivity();
  }

  final class ViewModel extends ActivityViewModel<ProfileActivity> implements ProfileAdapter.Delegate, Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<User> freshUser = this.client.fetchCurrentUser()
        .retry(2)
        .compose(Transformers.neverError());
      freshUser.subscribe(this.currentUser::refresh);

      final DiscoveryParams params = DiscoveryParams.builder()
        .backed(1)
        .perPage(18)
        .sort(DiscoveryParams.Sort.ENDING_SOON)
        .build();

      final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
        ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
          .nextPage(this.nextPage)
          .envelopeToListOfData(DiscoverEnvelope::projects)
          .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
          .loadWithParams(__ -> this.client.fetchProjects(params))
          .loadWithPaginationPath(this.client::fetchProjects)
          .build();

      paginator.paginatedData()
        .compose(bindToLifecycle())
        .subscribe(this.projects::onNext);

      this.koala.trackProfileView();
    }

    private final PublishSubject<Void> nextPage = PublishSubject.create();

    private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
    private final PublishSubject<Project> showProject = PublishSubject.create();
    private final PublishSubject<Void> showDiscovery = PublishSubject.create();

    public final ProfileViewModel.Inputs inputs = this;
    public final ProfileViewModel.Outputs outputs = this;

    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void profileCardViewHolderClicked(final @NonNull ProfileCardViewHolder viewHolder, final @NonNull Project project) {
      this.showProject.onNext(project);
    }
    @Override public void emptyProfileViewHolderExploreProjectsClicked(final @NonNull EmptyProfileViewHolder viewHolder) {
      this.showDiscovery.onNext(null);
    }

    @Override public @NonNull Observable<List<Project>> projects() {
      return this.projects;
    }
    @Override public @NonNull Observable<User> user() {
      return this.currentUser.loggedInUser();
    }
    @Override public @NonNull Observable<Project> startProjectActivity() {
      return this.showProject;
    }
    @Override public @NonNull Observable<Void> resumeDiscoveryActivity() {
      return this.showDiscovery;
    }
  }
}
