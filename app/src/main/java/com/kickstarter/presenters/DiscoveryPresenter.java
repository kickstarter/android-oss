package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.ListUtils;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class DiscoveryPresenter extends Presenter<DiscoveryActivity> implements DiscoveryAdapter.Delegate {
  @Inject ApiClient apiClient;
  @Inject KickstarterClient kickstarterClient;
  @Inject BuildCheck buildCheck;

  private final PublishSubject<Project> projectClick = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  private final PublishSubject<DiscoveryParams> params = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, kickstarterClient);

    final Observable<List<Project>> projects = params
      .switchMap(this::projectsWithPagination);

    final Observable<Pair<DiscoveryActivity, List<Project>>> viewAndProjects =
      RxUtils.combineLatestPair(viewSubject, projects);

    addSubscription(
      RxUtils.takeWhen(viewSubject, params).subscribe(DiscoveryActivity::clearItems)
    );

    addSubscription(viewAndProjects
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.loadProjects(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.startProjectActivity(vp.second))
    );

    // TODO: We shouldn't have to do this, but BehaviorSubject and scan
    // don't seem to play well together:
    // https://github.com/ReactiveX/RxJava/issues/3168
    // This apparently fixes it:
    // https://github.com/ReactiveX/RxJava/pull/3171
    params.onNext(DiscoveryParams.params());
    nextPage.onNext(null);
  }

  /**
   * Given the params for the first page of a discovery search, returns an
   * observable of pages of projects. A new page of projects is emitted
   * whenever `nextPage` emits.
   */
  private Observable<List<Project>> projectsWithPagination(final DiscoveryParams firstPageParams) {
    return paramsWithPagination(firstPageParams)
      .switchMap(this::projectsFromParams)
      .takeUntil(List::isEmpty)
      .scan(ListUtils::concatDistinct)
      ;
  }

  /**
   * Given the params for the first page of a discovery search, returns
   * an observable of params for each pagination. A new param is emitted
   * whenever `nextPage` emits.
   */
  private Observable<DiscoveryParams> paramsWithPagination(final DiscoveryParams firstPageParams) {
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
  private Observable<List<Project>> projectsFromParams(final DiscoveryParams params) {
    return apiClient.fetchProjects(params)
      .retry(2)
      .onErrorResumeNext(e -> Observable.empty())
      .map(envelope -> envelope.projects)
      .map(this::bumpPOTDToFront)
      ;
  }

  /**
   * Give a list of projects, finds if it contains the POTD and if so
   * bumps it to the front of the list.
   */
  private List<Project> bumpPOTDToFront(final List<Project> projects) {

    return Observable.from(projects)
      .reduce(new ArrayList<>(), (final List<Project> accum, final Project p) -> {
        return p.isPotdToday() ? ListUtils.prepend(accum, p) : ListUtils.append(accum, p);
      })
      .toBlocking().single();
  }

  public void projectClick(final ProjectCardViewHolder viewHolder, final Project project) {
    projectClick.onNext(project);
  }

  public void takeParams(final DiscoveryParams firstPageParams) {
    params.onNext(firstPageParams);
  }

  public void takeNextPage() {
    nextPage.onNext(null);
  }
}
