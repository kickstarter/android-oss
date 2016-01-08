package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ProfileActivity;
import com.kickstarter.viewmodels.inputs.ProfileViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProfileViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ProfileViewModel extends ViewModel<ProfileActivity> implements ProfileViewModelInputs, ProfileViewModelOutputs {
  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;

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
    return currentUser.observable();
  }

  public final ProfileViewModelInputs inputs = this;
  public final ProfileViewModelOutputs outputs = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

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

    addSubscription(paginator.paginatedData.subscribe(projects));

    koala.trackProfileView();
  }
}
