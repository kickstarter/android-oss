package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.HamburgerActivity;
import com.kickstarter.ui.adapters.NavigationDrawerAdapter;
import com.kickstarter.ui.viewholders.HamburgerNavigationChildFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationRootFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationTopFilterViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

public final class HamburgerViewModel extends ViewModel<HamburgerActivity> implements NavigationDrawerAdapter.Delegate {
  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUser currentUser;

  private BehaviorSubject<NavigationDrawerAdapter.Data> navigationDrawerData = BehaviorSubject.create();
  public Observable<NavigationDrawerAdapter.Data> navigationDrawerData() {
    return navigationDrawerData;
  }

  private PublishSubject<NavigationDrawerAdapter.Data.Section.Row> childFilterRowCLick = PublishSubject.create();
  @Override
  public void rowClick(@NonNull HamburgerNavigationChildFilterViewHolder viewHolder, @NonNull NavigationDrawerAdapter.Data.Section.Row row) {
    childFilterRowCLick.onNext(row);
  }

  private PublishSubject<NavigationDrawerAdapter.Data.Section.Row> rootFilterRowClick = PublishSubject.create();
  @Override
  public void rowClick(@NonNull HamburgerNavigationRootFilterViewHolder viewHolder, @NonNull NavigationDrawerAdapter.Data.Section.Row row) {
    rootFilterRowClick.onNext(row);
  }

  private PublishSubject<NavigationDrawerAdapter.Data.Section.Row> topFilterRowClick = PublishSubject.create();
  @Override
  public void rowClick(@NonNull HamburgerNavigationTopFilterViewHolder viewHolder, @NonNull NavigationDrawerAdapter.Data.Section.Row row) {
    topFilterRowClick.onNext(row);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(Transformers.neverError())
      .flatMap(Observable::from)
      .toSortedList()
      .share();

    PublishSubject<DiscoveryParams> selectedParams = PublishSubject.create();
    PublishSubject<DiscoveryParams> expandedParams = PublishSubject.create();
    Observable<Pair<DiscoveryParams, DiscoveryParams>> selectedAndExpandedParams = selectedParams
      .compose(combineLatestPair(expandedParams));

    Observable.combineLatest(categories, selectedParams, expandedParams, currentUser.observable(), HamburgerViewModel::magic)
      .subscribe(navigationDrawerData::onNext);

    selectedParams.onNext(DiscoveryParams.builder().staffPicks(true).build());
    expandedParams.onNext(null);

    rootFilterRowClick
      .map(NavigationDrawerAdapter.Data.Section.Row::params)
      .subscribe(expandedParams::onNext);
  }

  static NavigationDrawerAdapter.Data magic(List<Category> categories, DiscoveryParams selected, @Nullable DiscoveryParams expanded, User user) {

    NavigationDrawerAdapter.Data.Builder builder = NavigationDrawerAdapter.Data.builder();

    List<NavigationDrawerAdapter.Data.Section> categorySections = Observable.from(categories)
      .filter(c -> isVisible(c, expanded))
      .flatMap(c -> doubleRootIfExpanded(c, expanded))
      .map(c -> DiscoveryParams.builder().category(c).build())
      .toList()
      .map(HamburgerViewModel::massage)
      .flatMap(HamburgerViewModel::massageSections)
      .toBlocking().single();

    List<NavigationDrawerAdapter.Data.Section> sections = Observable
      .from(categorySections)
      .startWith(topSections(user))
      .toList().toBlocking().single();

    return builder.sections(sections)
      .user(user)
      .build();
  }

  static public List<List<DiscoveryParams>> massage(List<DiscoveryParams> ps) {
    TreeMap<String, List<DiscoveryParams>> grouped = new TreeMap<>();
    for (final DiscoveryParams p : ps) {
      if (!grouped.containsKey(p.category().root().name())) {
        grouped.put(p.category().root().name(), new ArrayList<>());
      }
      grouped.get(p.category().root().name()).add(p);
    }

    return new ArrayList<>(grouped.values());
  }

  static Observable<List<NavigationDrawerAdapter.Data.Section>> massageSections(List<List<DiscoveryParams>> sections) {

    return Observable.from(sections)
      .flatMap(HamburgerViewModel::massageRows)
      .map(rows -> NavigationDrawerAdapter.Data.Section.builder().rows(rows).build())
      .toList();
  }

  static Observable<List<NavigationDrawerAdapter.Data.Section.Row>> massageRows(List<DiscoveryParams> rows) {
    return Observable.from(rows)
      .map(p -> NavigationDrawerAdapter.Data.Section.Row.builder().params(p).build())
      .toList();
  }

  static boolean isVisible(Category category, @Nullable DiscoveryParams expandedParams) {
    if (expandedParams == null) {
      return category.isRoot();
    }
    final Category expandedCategory = expandedParams.category();
    if (expandedCategory == null) {
      return category.isRoot();
    }

    if (category.isRoot()) {
      return true;
    }

    return category.root().id() == expandedCategory.id();
  }

  static Observable<Category> doubleRootIfExpanded(Category category, @Nullable DiscoveryParams expandedParams) {
    if (expandedParams == null) {
      return Observable.just(category);
    }
    final Category expandedCategory = expandedParams.category();
    if (expandedCategory == null) {
      return Observable.just(category);
    }

    return category.isRoot() && category.id() == expandedCategory.id() ? Observable.just(category, category) : Observable.just(category);
  }

  static Observable<NavigationDrawerAdapter.Data.Section> topSections(@Nullable User user) {
    List<DiscoveryParams> filters = ListUtils.empty();

    filters.add(DiscoveryParams.builder().staffPicks(true).build());
    if (user != null) {
      filters.add(DiscoveryParams.builder().starred(1).build());
    }
    filters.add(DiscoveryParams.builder().build());

    return Observable.from(filters)
      .map(p -> NavigationDrawerAdapter.Data.Section.Row.builder().params(p).build())
      .map(Collections::singletonList)
      .map(rows -> NavigationDrawerAdapter.Data.Section.builder().rows(rows).build());
  }
}
