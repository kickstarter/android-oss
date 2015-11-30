package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.inputs.SearchViewModelInputs;
import com.kickstarter.viewmodels.outputs.SearchViewModelOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class SearchViewModel extends ViewModel<SearchActivity> implements SearchViewModelInputs, SearchViewModelOutputs {
  // INPUTS
  private final PublishSubject<String> search = PublishSubject.create();
  public SearchViewModelInputs inputs = this;
  @Override public void search(@NonNull final String s) { search.onNext(s); }

  // OUTPUTS
  private final PublishSubject<Empty> clearData = PublishSubject.create();
  private final PublishSubject<Pair<DiscoveryParams, List<Project>>> newData = PublishSubject.create();
  public final SearchViewModelOutputs outputs = this;
  @Override public Observable<Empty> clearData() { return clearData.asObservable(); }
  @Override public Observable<Pair<DiscoveryParams, List<Project>>> newData() { return newData.asObservable(); }

  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Project>> projects = params
      .switchMap(this::projects)
      .share();

    final Observable<Pair<DiscoveryParams, List<Project>>> paramsAndProjects = params
      .compose(Transformers.takePairWhen(projects))
      .share();

    final Observable<Boolean> isSearchEmpty = search.map(t -> t.length() == 0).share();

    final Observable<Pair<DiscoveryParams, List<Project>>> popularParamsAndProjects = paramsAndProjects.first();
    final Observable<Pair<DiscoveryParams, List<Project>>> searchParamsAndProjects = paramsAndProjects.skip(1);

    // When the search field changes, start a new search and clear results
    addSubscription(
      search
        .compose(Transformers.takeWhen(isSearchEmpty.filter(v -> !v)))
        .subscribe(text -> {
          params.onNext(DiscoveryParams.builder().term(text).build());
          clearData.onNext(Empty.get());
        })
    );

    // When the search field is empty (i.e. on load or when the search field is cleared), ping with the
    // popular projects.
    addSubscription(
      popularParamsAndProjects
        .compose(Transformers.combineLatestPair(isSearchEmpty))
        .filter(pe -> pe.second)
        .map(pe -> pe.first)
        .subscribe(newData)
    );

    // When we receive new search results and the search field is still not empty, ping with the search results
    addSubscription(
      isSearchEmpty
        .compose(Transformers.takePairWhen(searchParamsAndProjects))
        .filter(pe -> !pe.first)
        .map(pe -> pe.second)
        .debounce(500, TimeUnit.MILLISECONDS)
        .subscribe(newData)
    );

    // Track us viewing this page
    koala.trackSearchView();

    // Track search results and pagination
    final Observable<Integer> pageCount = params
      .switchMap(__ -> projects.debounce(2, TimeUnit.SECONDS).compose(Transformers.incrementalCount()));
    final Observable<String> query = searchParamsAndProjects.map(sp -> sp.first.term());
    addSubscription(query
      .compose(Transformers.takePairWhen(pageCount))
      .subscribe(qp -> koala.trackSearchResults(qp.first, qp.second))
    );

    // Start with popular projects
    params.onNext(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
  }

  private Observable<List<Project>> projects(@NonNull final DiscoveryParams newParams) {
    return apiClient.fetchProjects(newParams)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects);
  }
}
