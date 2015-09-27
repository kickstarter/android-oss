package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Category;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.DiscoveryFilterActivity;
import com.kickstarter.ui.adapters.DiscoveryFilterAdapter;
import com.kickstarter.ui.viewholders.DiscoveryFilterViewHolder;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.GroupedObservable;
import rx.subjects.PublishSubject;

public class DiscoveryFilterPresenter extends Presenter<DiscoveryFilterActivity> implements DiscoveryFilterAdapter.Delegate {
  @Inject ApiClient apiClient;
  private final PublishSubject<DiscoveryParams> discoveryFilterClick = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void initialize(final DiscoveryParams initialDiscoveryParams) {
    final Observable<List<DiscoveryParams>> discoveryParams = apiClient.fetchCategories()
      .map(this::categoriesToDiscoveryParams);

    final Observable<Pair<DiscoveryFilterActivity, List<DiscoveryParams>>> viewAndDiscoveryParams =
      RxUtils.combineLatestPair(viewSubject, discoveryParams);

    addSubscription(viewAndDiscoveryParams
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vd -> vd.first.loadDiscoveryParams(vd.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, discoveryFilterClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.startDiscoveryActivity(vp.second))
    );
  }

  public void discoveryFilterClick(final DiscoveryFilterViewHolder viewHolder, final DiscoveryParams discoveryParams) {
    discoveryFilterClick.onNext(discoveryParams);
  }

  protected List<DiscoveryParams> categoriesToDiscoveryParams(final List<Category> initialCategories) {
    final Observable<Category> categories = Observable.from(initialCategories)
      .toSortedList(Category::discoveryFilterCompareTo)
      .flatMap(Observable::from);

    final Observable<GroupedObservable<Long, Category>> groupedCategories = categories.groupBy(Category::rootId);

    // TODO: Add social sort when there is a current user
    final Observable<DiscoveryParams> discoveryParams = Observable.concat(groupedCategories)
      .map(c -> DiscoveryParams.builder().category(c).build())
      .startWith(
        DiscoveryParams.builder().staffPicks(true).build(),
        DiscoveryParams.builder().starred(1).build(),
        DiscoveryParams.builder().build() // Everything sort
      );

    return discoveryParams.toList().toBlocking().single();
  }
}
