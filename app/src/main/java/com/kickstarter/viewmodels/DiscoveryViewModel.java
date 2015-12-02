package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class DiscoveryViewModel extends ViewModel<DiscoveryActivity> implements DiscoveryViewModelInputs {
  // INPUTS
  private final PublishSubject<Project> projectClick = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }

  @Inject ApiClient apiClient;
  @Inject WebClient webClient;
  @Inject BuildCheck buildCheck;

  private final PublishSubject<Void> filterButtonClick = PublishSubject.create();
  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  public final DiscoveryViewModelInputs inputs = this;

  @Override
  public void projectClick(@NonNull final Project project) {
    projectClick.onNext(project);
  }

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
        .subscribe(koala::trackDiscovery)
    );

    final Observable<List<Project>> projects = paginator.paginatedData;

    final Observable<Pair<DiscoveryActivity, List<Project>>> viewAndProjects = view
      .compose(Transformers.combineLatestPair(projects));

    final Observable<Pair<DiscoveryActivity, DiscoveryParams>> viewAndParams = view
      .compose(Transformers.combineLatestPair(params));

    addSubscription(viewAndParams
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.loadParams(vp.second)));

    addSubscription(viewAndProjects
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.loadProjects(vp.second)));

    addSubscription(
      viewAndParams
        .compose(Transformers.takeWhen(filterButtonClick))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.startDiscoveryFilterActivity(vp.second))
    );

    addSubscription(
      view
        .compose(Transformers.takePairWhen(projectClick))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.startProjectActivity(vp.second))
    );

    params.onNext(DiscoveryParams.builder().staffPicks(true).build());
  }


  /**
   * Given params for a discovery search, returns an observable of the
   * page of projects received from the api.
   *
   * Note: This ignores any api errors.
   */
  private Observable<List<Project>> projectsFromParams(@NonNull final DiscoveryParams params) {
    return apiClient.fetchProjects(params)
      .retry(2)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects)
      .map(this::bringPotdToFront);
  }

  /**
   * Given a list of projects, finds if it contains the POTD and if so
   * bumps it to the front of the list.
   */
  private List<Project> bringPotdToFront(@NonNull final List<Project> projects) {

    return Observable.from(projects)
      .reduce(new ArrayList<>(), this::prependPotdElseAppend)
      .toBlocking().single();
  }

  /**
   * Given a list of projects and a particular project, returns the list
   * when the project prepended if it's POTD and appends otherwise.
   */
  @NonNull private List<Project> prependPotdElseAppend(@NonNull final List<Project> projects, @NonNull final Project project) {
    return project.isPotdToday() ? ListUtils.prepend(projects, project) : ListUtils.append(projects, project);
  }

  public void filterButtonClick() {
    filterButtonClick.onNext(null);
  }

  public void takeParams(@NonNull final DiscoveryParams firstPageParams) {
    params.onNext(firstPageParams);
  }
}
