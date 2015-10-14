package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;
import com.kickstarter.ui.adapters.SearchAdapter;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class SearchPresenter extends Presenter<SearchActivity> implements SearchAdapter.Delegate {
  @Inject ApiClient apiClient;
  private final PublishSubject<Project> projectSearchResultClick = PublishSubject.create();
  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Project>> projects = params
      .switchMap(this::projects);

    final Observable<Pair<SearchActivity, List<Project>>> viewAndProjects =
      RxUtils.combineLatestPair(viewSubject, projects);

    final Observable<Pair<SearchActivity, DiscoveryParams>> viewAndParams =
      RxUtils.combineLatestPair(viewSubject, params);

    addSubscription(viewAndProjects
      .filter(v -> v != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.loadProjects(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectSearchResultClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectIntent(vp.second)));

    params.onNext(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build());
  }

  @Override
  public void projectSearchResultClick(@NonNull final ProjectSearchResultViewHolder viewHolder, @NonNull final Project project) {
    projectSearchResultClick.onNext(project);
  }

  private Observable<List<Project>> projects(@NonNull final DiscoveryParams initialParams) {
    return apiClient.fetchProjects(initialParams)
      .map(DiscoverEnvelope::projects);
  }
}
