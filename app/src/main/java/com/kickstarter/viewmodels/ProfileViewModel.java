package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.viewmodels.inputs.ProfileViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProfileViewModelOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class ProfileViewModel extends ViewModel<ProfileActivity> implements ProfileViewModelInputs, ProfileViewModelOutputs {
  @Inject ApiClient apiClient;
  @Inject CurrentUser currentUser;

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }

  // OUTPUTS
  private final PublishSubject<List<Project>> projects = PublishSubject.create();
  @Override public Observable<List<Project>> projects() {
    return projects.asObservable();
  }

  @Override public Observable<User> user() {
    return currentUser.observable();
  }

  public final ProfileViewModelInputs inputs = this;
  public final ProfileViewModelOutputs outputs = this;

  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Project>> backedProjects = params.switchMap(this::projectsWithPagination);

    addSubscription(view
        .compose(Transformers.combineLatestPair(backedProjects))
        .subscribe(vp -> projects.onNext(vp.second))
    );

    final DiscoveryParams firstPageParams = DiscoveryParams.builder()
      .backed(1)
      .sort(DiscoveryParams.Sort.ENDING_SOON)
      .build();

    params.onNext(firstPageParams);

    koala.trackProfileView();
  }

  private Observable<List<Project>> projectsWithPagination(@NonNull final DiscoveryParams firstPageParams) {
    return paramsWithPagination(firstPageParams)
      .concatMap(this::projectsFromParams)
      .takeUntil(List::isEmpty)
      .scan(new ArrayList<>(), ListUtils::concatDistinct);
  }

  private Observable<DiscoveryParams> paramsWithPagination(@NonNull final DiscoveryParams firstPageParams) {
    return nextPage
      .scan(firstPageParams, (currentPage, __) -> currentPage.nextPage());
  }

  private Observable<List<Project>> projectsFromParams(@NonNull final DiscoveryParams params) {
    return apiClient.fetchProjects(params)
      .retry(2)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects);
  }
}
