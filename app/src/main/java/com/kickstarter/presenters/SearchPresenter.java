package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;
import com.kickstarter.ui.adapters.SearchAdapter;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class SearchPresenter extends Presenter<SearchActivity> implements SearchAdapter.Delegate {
  @Inject ApiClient apiClient;
  private final PublishSubject<Project> projectSearchResultClick = PublishSubject.create();
  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<CharSequence> searchText = viewChange
      .filter(v -> v != null)
      .flatMap(v -> RxTextView.textChanges(v.toolbar.searchEditText));

    final Observable<List<Project>> projects = params
      .switchMap(this::projects);

    final Observable<Pair<DiscoveryParams, List<Project>>> paramsAndProjects = RxUtils.takePairWhen(params, projects);

    final Observable<Boolean> isSearchEmpty = searchText.map(t -> t.length() == 0).share();

    final Observable<List<?>> viewParamsProjects = RxUtils.takePairWhen(viewChange, paramsAndProjects)
      .filter(vp -> vp.first != null)
      .map(vp -> Arrays.asList(vp.first, vp.second.first, vp.second.second));

    addSubscription(RxUtils.takeWhen(viewChange, searchText)
      .filter(v -> v != null)
      .skip(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.adapter().clear()));

    addSubscription(RxUtils.takeWhen(searchText, isSearchEmpty.filter(b -> !b))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(text -> params.onNext(DiscoveryParams.builder().term(text.toString()).build())));

    addSubscription(viewParamsProjects
      .take(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadData));

    addSubscription(RxUtils.takePairWhen(isSearchEmpty, viewParamsProjects)
      .filter(evpp -> !evpp.first)
      .map(evpp -> evpp.second)
      .debounce(500, TimeUnit.MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadData)
    );

    addSubscription(RxUtils.takePairWhen(viewChange, projectSearchResultClick)
      .filter(vp -> vp.first != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectIntent(vp.second)));

    params.onNext(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
  }

  @Override
  public void projectSearchResultClick(@NonNull final ProjectSearchResultViewHolder viewHolder, @NonNull final Project project) {
    projectSearchResultClick.onNext(project);
  }

  private Observable<List<Project>> projects(@NonNull final DiscoveryParams newParams) {
    return apiClient.fetchProjects(newParams)
      .map(DiscoverEnvelope::projects);
  }

  private void loadData(final List<?> viewProjectsParams) {
    final SearchActivity view = (SearchActivity) viewProjectsParams.get(0);
    final DiscoveryParams params = (DiscoveryParams) viewProjectsParams.get(1);
    final List<Project> ps = (List<Project>) viewProjectsParams.get(2);
    view.adapter().loadProjectsAndParams(ps, params);
  }
}
