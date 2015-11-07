package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.inputs.DiscoveryPresenterInputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class DiscoveryPresenter extends Presenter<DiscoveryActivity> implements DiscoveryPresenterInputs {
  // INPUTS
  private final PublishSubject<Project> projectClick = PublishSubject.create();
  private final PublishSubject<Void> scrollEvent = PublishSubject.create();

  @Inject ApiClient apiClient;
  @Inject WebClient webClient;
  @Inject BuildCheck buildCheck;

  private final PublishSubject<Void> filterButtonClick = PublishSubject.create();
  private final PublishSubject<Empty> nextPage = PublishSubject.create();
  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  public final DiscoveryPresenterInputs inputs = this;

  @Override
  public void projectClick(@NonNull final Project project) {
    projectClick.onNext(project);
  }

  @Override
  public void scrollEvent() {
    scrollEvent.onNext(null);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, webClient);

    final Observable<List<Project>> projects = params
      .switchMap(this::projectsWithPagination);

    final Observable<Pair<DiscoveryActivity, List<Project>>> viewAndProjects =
      RxUtils.combineLatestPair(viewSubject, projects);

    final Observable<Pair<DiscoveryActivity, DiscoveryParams>> viewAndParams =
      RxUtils.combineLatestPair(viewSubject, params);

    final Observable<Pair<Integer, Integer>> visibleItemOfTotal = viewChange
      .compose(Transformers.takeWhen(scrollEvent))
      .filter(v -> v != null)
      .map(v -> v.recyclerView)
      .map(RecyclerView::getLayoutManager)
      .ofType(LinearLayoutManager.class)
      .map(this::displayedItemFromLayout);

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
      viewSubject
        .compose(Transformers.takePairWhen(projectClick))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.startProjectActivity(vp.second))
    );

    addSubscription(visibleItemOfTotal
      .distinctUntilChanged()
      .filter(this::isCloseToBottom)
      .subscribe(__ -> nextPage.onNext(null))
    );

    params.onNext(DiscoveryParams.builder().staffPicks(true).build());
  }

  /**
   * Returns a (visibleItem, totalItemCount) pair given a linear layout manager.
   * TODO: This would need to be improved to handle grid layouts if we use those in the future.
   */
  private Pair<Integer, Integer> displayedItemFromLayout(@NonNull final LinearLayoutManager manager) {
    final int visibleItemCount = manager.getChildCount();
    final int totalItemCount = manager.getItemCount();
    final int pastVisibleItems = manager.findFirstVisibleItemPosition();
    return new Pair<>(visibleItemCount + pastVisibleItems, totalItemCount);
  }

  /**
   * Returns `true` when the visible item gets "close" to the bottom.
   */
  private boolean isCloseToBottom(@NonNull final Pair<Integer, Integer> visibleItemOfTotal) {
    return visibleItemOfTotal.first == visibleItemOfTotal.second - 2;
  }

  /**
   * Given the params for the first page of a discovery search, returns an
   * observable of pages of projects. A new page of projects is emitted
   * whenever `nextPage` emits.
   */
  private Observable<List<Project>> projectsWithPagination(@NonNull final DiscoveryParams firstPageParams) {
    return paramsWithPagination(firstPageParams)
      .concatMap(this::projectsFromParams)
      .takeUntil(List::isEmpty)
      .scan(ListUtils::concatDistinct)
      ;
  }

  /**
   * Given the params for the first page of a discovery search, returns
   * an observable of params for each pagination. A new param is emitted
   * whenever `nextPage` emits.
   */
  private Observable<DiscoveryParams> paramsWithPagination(@NonNull final DiscoveryParams firstPageParams) {
    return nextPage
      .scan(firstPageParams, (currentPage, __) -> currentPage.nextPage())
      ;
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
      .map(this::bumpPOTDToFront)
      ;
  }

  /**
   * Give a list of projects, finds if it contains the POTD and if so
   * bumps it to the front of the list.
   */
  private List<Project> bumpPOTDToFront(@NonNull final List<Project> projects) {

    return Observable.from(projects)
      .reduce(new ArrayList<>(), (final List<Project> accum, final Project p) -> {
        return p.isPotdToday() ? ListUtils.prepend(accum, p) : ListUtils.append(accum, p);
      })
      .toBlocking().single();
  }

  public void filterButtonClick() {
    filterButtonClick.onNext(null);
  }

  public void takeParams(@NonNull final DiscoveryParams firstPageParams) {
    params.onNext(firstPageParams);
  }
}
