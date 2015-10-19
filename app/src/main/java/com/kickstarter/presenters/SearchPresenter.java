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
import rx.subjects.PublishSubject;

public class SearchPresenter extends Presenter<SearchActivity> implements SearchPresenterInputs, SearchPresenterOutputs {
  // INPUTS
  private final PublishSubject<String> searchSubject = PublishSubject.create();
  public SearchPresenterInputs inputs() { return this; }
  @Override public void search(@NonNull final String s) { searchSubject.onNext(s); }

  // OUTPUTS
  private final PublishSubject<Empty> clearSubject = PublishSubject.create();
  private final PublishSubject<Pair<DiscoveryParams, List<Project>>> newDataSubject = PublishSubject.create();
  public SearchPresenterOutputs outputs() { return this; }
  @Override public Observable<Empty> clear() { return clearSubject.asObservable(); }
  @Override public Observable<Pair<DiscoveryParams, List<Project>>> newData() { return newDataSubject.asObservable(); }

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

    final Observable<Boolean> isSearchEmpty = searchSubject.map(t -> t.length() == 0).share();

   // Search subject pings on load - we want to skip this to show initial results.
    addSubscription(searchSubject
      .skip(1)
      .subscribe(__ -> clearSubject.onNext(Empty.create())));

    addSubscription(RxUtils.takeWhen(searchSubject, isSearchEmpty.filter(b -> !b))
      .subscribe(text -> paramsSubject.onNext(DiscoveryParams.builder().term(text).build())));

    // Just show the first ping with no filtering
    addSubscription(paramsAndProjects
      .take(1)
      .subscribe(pp -> newDataSubject.onNext(pp)));

    // For subsequent pings, only want to show results if search is not empty. Search could have been cleared in
    // the time between when the API request was triggered and when it returned.
    addSubscription(RxUtils.takePairWhen(isSearchEmpty, paramsAndProjects)
      .filter(epp -> !epp.first)
      .map(evpp -> evpp.second)
      .debounce(500, TimeUnit.MILLISECONDS)
      .subscribe(pp -> newDataSubject.onNext(pp)));

    // Start with popular projects.
    paramsSubject.onNext(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
  }

  private Observable<List<Project>> projects(@NonNull final DiscoveryParams newParams) {
    return apiClient.fetchProjects(newParams)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects);
  }
}
