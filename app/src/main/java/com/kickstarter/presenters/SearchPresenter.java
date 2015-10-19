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
  private final PublishSubject<Project> projectClick = PublishSubject.create();
  private final PublishSubject<String> search = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Empty> clear = PublishSubject.create();
  private final PublishSubject<Project> startProjectActivity = PublishSubject.create();
  private final PublishSubject<Pair<DiscoveryParams, List<Project>>> newData = PublishSubject.create();

  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Project>> projects = params
      .switchMap(this::projects)
      .share();

    final Observable<Pair<DiscoveryParams, List<Project>>> paramsAndProjects = RxUtils.takePairWhen(params, projects);

    final Observable<Boolean> isSearchEmpty = search.map(t -> t.length() == 0).share();

    addSubscription(search
      .skip(1) // Don't send clear signal for text change on load
      .subscribe(__ -> clear.onNext(Empty.create())));

    addSubscription(RxUtils.takeWhen(search, isSearchEmpty.filter(b -> !b))
      .subscribe(text -> params.onNext(DiscoveryParams.builder().term(text).build())));

    addSubscription(paramsAndProjects
      .take(1)
      .subscribe(pp -> newData.onNext(pp)));

    addSubscription(RxUtils.takePairWhen(isSearchEmpty, paramsAndProjects)
      .filter(epp -> !epp.first)
      .map(evpp -> evpp.second)
      .debounce(500, TimeUnit.MILLISECONDS)
      .subscribe(pp -> newData.onNext(pp)));

    addSubscription(projectClick
      .subscribe(startProjectActivity));

    params.onNext(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
  }

  public SearchPresenterInputs inputs() {
    return this;
  }

  @Override
  public void projectClick(@NonNull final Project project) {
    projectClick.onNext(project);
  }

  @Override
  public void search(@NonNull final String s) {
    search.onNext(s);
  }

  public SearchPresenterOutputs outputs() {
    return this;
  }

  @Override
  public Observable<Empty> clear() {
    return clear;
  }

  @Override
  public Observable<Project> startProjectActivity() {
    return startProjectActivity;
  }

  @Override
  public Observable<Pair<DiscoveryParams, List<Project>>> newData() {
    return newData;
  }

  private Observable<List<Project>> projects(@NonNull final DiscoveryParams newParams) {
    return apiClient.fetchProjects(newParams)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects);
  }
}
