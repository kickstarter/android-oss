package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ActivityViewModel;
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
import com.kickstarter.viewmodels.inputs.ProfileViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProfileViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ProfileViewModel extends ActivityViewModel<ProfileActivity> implements ProfileAdapter.Delegate, ProfileViewModelInputs, ProfileViewModelOutputs {
  private final ApiClientType client;
  private final CurrentUserType currentUser;

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  @Override public Observable<List<Project>> projects() {
    return projects;
  }
  @Override public Observable<User> user() {
    return currentUser.loggedInUser();
  }
  private final PublishSubject<Project> showProject = PublishSubject.create();
  @Override
  public Observable<Project> showProject() {
    return showProject;
  }
  private final PublishSubject<Void> showDiscovery = PublishSubject.create();
  @Override
  public Observable<Void> showDiscovery() {
    return showDiscovery;
  }

  public final ProfileViewModelInputs inputs = this;
  public final ProfileViewModelOutputs outputs = this;

  public ProfileViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();
    currentUser = environment.currentUser();

    final Observable<User> freshUser = client.fetchCurrentUser()
      .retry(2)
      .compose(Transformers.neverError());
    freshUser.subscribe(currentUser::refresh);

    final DiscoveryParams params = DiscoveryParams.builder()
      .backed(1)
      .perPage(18)
      .sort(DiscoveryParams.Sort.ENDING_SOON)
      .build();

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(nextPage)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .loadWithParams(__ -> client.fetchProjects(params))
        .loadWithPaginationPath(client::fetchProjects)
        .build();

    paginator.paginatedData()
      .compose(bindToLifecycle())
      .subscribe(projects::onNext);

    koala.trackProfileView();
  }

  public void profileCardViewHolderClicked(final @NonNull ProfileCardViewHolder viewHolder, final @NonNull Project project) {
    this.showProject.onNext(project);
  }

  public void emptyProfileViewHolderExploreProjectsClicked(final @NonNull EmptyProfileViewHolder viewHolder) {
    this.showDiscovery.onNext(null);
  }
}
