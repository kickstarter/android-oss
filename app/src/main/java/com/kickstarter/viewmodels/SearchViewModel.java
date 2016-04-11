package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;
import com.kickstarter.viewmodels.inputs.SearchViewModelInputs;
import com.kickstarter.viewmodels.outputs.SearchViewModelOutputs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class SearchViewModel extends ViewModel<SearchActivity> implements SearchViewModelInputs, SearchViewModelOutputs {
  // INPUTS
  private final PublishSubject<String> search = PublishSubject.create();
  @Override public void search(final @NonNull String s) {
    search.onNext(s);
  }
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> popularProjects = BehaviorSubject.create();
  @Override public Observable<List<Project>> popularProjects() {
    return popularProjects;
  }
  private final BehaviorSubject<List<Project>> searchProjects = BehaviorSubject.create();
  @Override public Observable<List<Project>> searchProjects() {
    return searchProjects;
  }

  private static final DiscoveryParams defaultParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build();

  public final SearchViewModelInputs inputs = this;
  public final SearchViewModelOutputs outputs = this;

  public SearchViewModel(final @NonNull Environment environment) {
    super(environment);

    final ApiClientType apiClient = environment.apiClient();
    final Scheduler scheduler = environment.scheduler();

    final Observable<DiscoveryParams> searchParams = search
      .filter(StringUtils::isPresent)
      .debounce(300, TimeUnit.MILLISECONDS, scheduler)
      .map(s -> DiscoveryParams.builder().term(s).build());

    final Observable<DiscoveryParams> popularParams = search
      .filter(StringUtils::isEmpty)
      .map(__ -> defaultParams)
      .startWith(defaultParams);

    final Observable<DiscoveryParams> params = Observable.merge(searchParams, popularParams);

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(nextPage)
        .startOverWith(params)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .clearWhenStartingOver(true)
        .concater(ListUtils::concatDistinct)
        .loadWithParams(apiClient::fetchProjects)
        .loadWithPaginationPath(apiClient::fetchProjects)
        .build();

    search
      .filter(StringUtils::isEmpty)
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        searchProjects.onNext(ListUtils.empty());
      });

    params
      .compose(Transformers.takePairWhen(paginator.paginatedData()))
      .compose(bindToLifecycle())
      .subscribe(paramsAndProjects -> {
        if (paramsAndProjects.first.sort() == DiscoveryParams.Sort.POPULAR) {
          popularProjects.onNext(paramsAndProjects.second);
        } else {
          searchProjects.onNext(paramsAndProjects.second);
        }
      });

    // Track us viewing this page
    koala.trackSearchView();

    // Track search results and pagination
    final Observable<Integer> pageCount = paginator.loadingPage();
    final Observable<String> query = params
      .map(DiscoveryParams::term);
    query
      .compose(Transformers.takePairWhen(pageCount))
      .filter(qp -> StringUtils.isPresent(qp.first))
      .compose(bindToLifecycle())
      .subscribe(qp -> koala.trackSearchResults(qp.first, qp.second));
  }
}
