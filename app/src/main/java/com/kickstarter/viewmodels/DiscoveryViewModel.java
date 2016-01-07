package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryViewModelOutputs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class DiscoveryViewModel extends ViewModel<DiscoveryActivity> implements DiscoveryViewModelInputs, DiscoveryViewModelOutputs {
  @Inject ApiClient apiClient;
  @Inject WebClient webClient;
  @Inject BuildCheck buildCheck;
  @Inject CurrentUser currentUser;

  // INPUTS
  private final PublishSubject<Project> projectClicked = PublishSubject.create();
  @Override
  public void projectClicked(@NonNull final Project project) {
    projectClicked.onNext(project);
  }
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }
  private final PublishSubject<Void> filterButtonClicked = PublishSubject.create();
  public void filterButtonClicked() {
    filterButtonClicked.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  @Override
  public Observable<List<Project>> projects() {
    return projects;
  }
  private final BehaviorSubject<DiscoveryParams> params = BehaviorSubject.create();
  @Override
  public Observable<DiscoveryParams> params() {
    return params;
  }
  private final BehaviorSubject<Boolean> shouldShowOnboarding = BehaviorSubject.create();
  @Override
  public Observable<Boolean> shouldShowOnboarding() {
    return shouldShowOnboarding;
  }
  @Override
  public Observable<DiscoveryParams> showFilters() {
    return params.compose(Transformers.takeWhen(filterButtonClicked));
  }
  @Override
  public Observable<Project> showProject() {
    return projectClicked;
  }

  private boolean hasSeenOnboarding = false;

  public final DiscoveryViewModelInputs inputs = this;
  public final DiscoveryViewModelOutputs outputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, webClient);

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(nextPage)
        .startOverWith(params)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .loadWithParams(apiClient::fetchProjects)
        .loadWithPaginationPath(apiClient::fetchProjects)
        .pageTransformation(this::bringPotdToFront)
        .clearWhenStartingOver(true)
        .concater(ListUtils::concatDistinct)
        .build();

    addSubscription(
      params.compose(Transformers.takePairWhen(paginator.loadingPage))
        .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
        .subscribe(p -> koala.trackDiscovery(p, !hasSeenOnboarding))
    );

    addSubscription(paginator.paginatedData.subscribe(projects));

    addSubscription(
      params
        .compose(Transformers.combineLatestPair(currentUser.isLoggedIn()))
        .map(pu -> isOnboardingVisible(pu.first, pu.second))
        .doOnNext(show -> hasSeenOnboarding = show || hasSeenOnboarding)
        .subscribe(shouldShowOnboarding::onNext)
    );

    params.onNext(DiscoveryParams.builder().staffPicks(true).build());
  }

  /**
   * Given a list of projects, finds if it contains the POTD and if so
   * bumps it to the front of the list.
   */
  private List<Project> bringPotdToFront(final @NonNull List<Project> projects) {

    return Observable.from(projects)
      .reduce(new ArrayList<>(), this::prependPotdElseAppend)
      .toBlocking().single();
  }

  /**
   * Given a list of projects and a particular project, returns the list
   * when the project prepended if it's POTD and appends otherwise.
   */
  @NonNull private List<Project> prependPotdElseAppend(final @NonNull List<Project> projects, final @NonNull Project project) {
    return project.isPotdToday() ? ListUtils.prepend(projects, project) : ListUtils.append(projects, project);
  }

  private boolean isOnboardingVisible(final @NonNull DiscoveryParams currentParams, final boolean isLoggedIn) {
    return !isLoggedIn && !hasSeenOnboarding && currentParams.staffPicks();
  }

  public void takeParams(final @NonNull DiscoveryParams firstPageParams) {
    params.onNext(firstPageParams);
  }
}
