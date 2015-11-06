package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.inputs.SearchPresenterInputs;
import com.kickstarter.presenters.outputs.SearchPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class SearchPresenter extends Presenter<SearchActivity> implements SearchPresenterInputs, SearchPresenterOutputs {
  // INPUTS
  private final PublishSubject<String> search = PublishSubject.create();
  public SearchPresenterInputs inputs() { return this; }
  @Override public void search(@NonNull final String s) { search.onNext(s); }

  // OUTPUTS
  private final PublishSubject<Empty> clear = PublishSubject.create();
  private final PublishSubject<Pair<DiscoveryParams, List<Project>>> newData = PublishSubject.create();
  public SearchPresenterOutputs outputs() { return this; }
  @Override public Observable<Empty> clear() { return clear.asObservable(); }
  @Override public Observable<Pair<DiscoveryParams, List<Project>>> newData() { return newData.asObservable(); }

  private final PublishSubject<DiscoveryParams> paramsSubject = PublishSubject.create();

  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Project>> projects = paramsSubject
      .switchMap(this::projects)
      .share();

    final Observable<Pair<DiscoveryParams, List<Project>>> paramsAndProjects = RxUtils.takePairWhen(paramsSubject, projects);

    final Observable<Boolean> isSearchEmpty = search.map(t -> t.length() == 0).share();

    final Observable<Pair<DiscoveryParams, List<Project>>> popularParamsAndProjects = paramsAndProjects.first();
    final Observable<Pair<DiscoveryParams, List<Project>>> searchParamsAndProjects = paramsAndProjects.skip(1);

    // When the search field is empty (i.e. on load or when the search field is cleared), ping with the
    // popular projects.
    addSubscription(RxUtils.combineLatestPair(popularParamsAndProjects, isSearchEmpty)
      .filter(pe -> pe.second)
      .map(pe -> pe.first)
      .subscribe(pp -> newData.onNext(pp))
    );

    // When the search field changes, clear results and start a new search
    addSubscription(RxUtils.takeWhen(search, isSearchEmpty.filter(v -> !v))
      .subscribe(text -> {
        clear.onNext(Empty.create());
        paramsSubject.onNext(DiscoveryParams.builder().term(text).build());
      }));

    // When we receive new search results and the search field is still not empty, ping with the search results
    addSubscription(RxUtils.takePairWhen(isSearchEmpty, searchParamsAndProjects)
      .filter(pe -> !pe.first)
      .map(pe -> pe.second)
      .debounce(500, TimeUnit.MILLISECONDS)
      .subscribe(pp -> newData.onNext(pp))
    );

    // Start with popular projects
    paramsSubject.onNext(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
  }

  private Observable<List<Project>> projects(@NonNull final DiscoveryParams newParams) {
    return apiClient.fetchProjects(newParams)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects);
  }
}
