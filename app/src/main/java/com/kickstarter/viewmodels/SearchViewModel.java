package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
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
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class SearchViewModel extends ViewModel<SearchActivity> implements SearchViewModelInputs, SearchViewModelOutputs {
  // INPUTS
  private final BehaviorSubject<String> search = BehaviorSubject.create("");
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() { nextPage.onNext(null); }
  public SearchViewModelInputs inputs = this;
  @Override public void search(final @NonNull String s) {
    search.onNext(s);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> popularProjects = BehaviorSubject.create();
  private final BehaviorSubject<List<Project>> searchProjects = BehaviorSubject.create();
  public final SearchViewModelOutputs outputs = this;
  @Override public Observable<List<Project>> popularProjects() { return popularProjects; }
  @Override public Observable<List<Project>> searchProjects() { return searchProjects; }

  private final DiscoveryParams defaultParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build();

  private final ApiClientType apiClient;

  public SearchViewModel(final @NonNull Environment environment) {
    super(environment);

    apiClient = environment.apiClient();
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    final Observable<DiscoveryParams> params = search
      .map(this::paramsFromSearch)
      .debounce(300, TimeUnit.MILLISECONDS);

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
      .compose(bindToLifecycle())
      .subscribe(__ -> searchProjects.onNext(ListUtils.empty()));

    params
      .compose(Transformers.takePairWhen(paginator.paginatedData))
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
    final Observable<Integer> pageCount = paginator.loadingPage;
    final Observable<String> query = params
      .filter(p -> p.sort() == DiscoveryParams.Sort.POPULAR)
      .map(DiscoveryParams::term);
    query
      .compose(Transformers.takePairWhen(pageCount))
      .compose(bindToLifecycle())
      .subscribe(qp -> koala.trackSearchResults(qp.first, qp.second));
  }

  private @NonNull DiscoveryParams paramsFromSearch(final @NonNull String search) {
    if (search.trim().isEmpty()) {
      return defaultParams;
    } else {
      return DiscoveryParams.builder().term(search).build();
    }
  }
}
