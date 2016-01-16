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
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class HamburgerViewModel extends ViewModel<HamburgerActivity> implements DiscoveryDrawerAdapter.Delegate {
  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUser currentUser;

  // ADAPTER DELEGATE INPUTS
  private PublishSubject<NavigationDrawerData.Section.Row> childFilterRowClick = PublishSubject.create();
  @Override
  public void rowClick(@NonNull ChildFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    childFilterRowClick.onNext(row);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> parentFilterRowClick = PublishSubject.create();
  @Override
  public void rowClick(@NonNull ParentFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    parentFilterRowClick.onNext(row);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> topFilterRowClick = PublishSubject.create();
  @Override
  public void rowClick(@NonNull TopFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    topFilterRowClick.onNext(row);
  }

  // INPUTS

  // OUTPUTS
  private BehaviorSubject<NavigationDrawerData> navigationDrawerData = BehaviorSubject.create();
  public Observable<NavigationDrawerData> navigationDrawerData() {
    return navigationDrawerData;
  }

  private BehaviorSubject<Boolean> openDrawer = BehaviorSubject.create(false);
  public Observable<Boolean> openDrawer() {
    return openDrawer;
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
    PublishSubject<Category> expandedParams = PublishSubject.create();

    Observable.combineLatest(categories, selectedParams, expandedParams, currentUser.observable(), HamburgerViewModel::magic)
      .subscribe(navigationDrawerData::onNext);

    selectedParams.onNext(DiscoveryParams.builder().staffPicks(true).build());
    expandedParams.onNext(null);

    final Observable<Category> clickedCategory = parentFilterRowClick
      .map(NavigationDrawerData.Section.Row::params)
      .map(DiscoveryParams::category);

    addSubscription(
      childFilterRowClick
        .mergeWith(topFilterRowClick)
        .subscribe(__ -> openDrawer.onNext(false))
    );

    childFilterRowClick
      .mergeWith(topFilterRowClick)
      .map(NavigationDrawerData.Section.Row::params)
      .subscribe(selectedParams::onNext);

    navigationDrawerData
      .map(NavigationDrawerData::expandedCategory)
      .compose(Transformers.takePairWhen(clickedCategory))
      .map(expandedAndClickedCategory -> toggleExpandedCategory(expandedAndClickedCategory.first, expandedAndClickedCategory.second))
      .subscribe(expandedParams::onNext);
  }

  private static @Nullable Category toggleExpandedCategory(final @Nullable Category expandedCategory, final @NonNull Category clickedCategory) {
    if (expandedCategory != null && clickedCategory.id() == expandedCategory.id()) {
      return null;
    }
    return clickedCategory;
  }

  /**
   * Converts all the disparate data representing the state of the menu data into a `NavigationDrawerData` object
   * that can be used to populate a view.
   * @param categories The full list of categories that can be displayed.
   * @param selected The params that correspond to what is currently selected in the menu.
   * @param expandedCategory The category that correspond to what is currently expanded in the menu.
   * @param user The currently logged in user.
   */
  static NavigationDrawerData magic(final @NonNull List<Category> categories, final @NonNull DiscoveryParams selected, final @Nullable Category expandedCategory, final @Nullable User user) {

    final NavigationDrawerData.Builder builder = NavigationDrawerData.builder();

    List<NavigationDrawerData.Section> categorySections = Observable.from(categories)
      .filter(c -> isVisible(c, expandedCategory))
      .flatMap(c -> doubleRootIfExpanded(c, expandedCategory))
      .map(c -> DiscoveryParams.builder().category(c).build())
      .toList()
      .map(HamburgerViewModel::paramsGroupedByRootCategory)
      .map(sections -> massageSections(sections, expandedCategory))
      .toBlocking().single();

    final List<NavigationDrawerData.Section> sections = Observable
      .from(categorySections)
      .startWith(topSections(user))
      .toList().toBlocking().single();

    return builder
      .sections(sections)
      .user(user)
      .selectedParams(selected)
      .expandedCategory(expandedCategory)
      .build();
  }

  /**
   * Converts the full list of category discovery params into a grouped list of params. A group corresponds to a root
   * category, and the list contains all subcategories.
   */
  static public List<List<DiscoveryParams>> paramsGroupedByRootCategory(final @NonNull List<DiscoveryParams> ps) {
    TreeMap<String, List<DiscoveryParams>> grouped = new TreeMap<>();
    for (final DiscoveryParams p : ps) {
      if (!grouped.containsKey(p.category().root().name())) {
        grouped.put(p.category().root().name(), new ArrayList<>());
      }
      grouped.get(p.category().root().name()).add(p);
    }

    return new ArrayList<>(grouped.values());
  }

  /**
   *
   * @param sections
   * @return
   */
  static List<NavigationDrawerData.Section> massageSections(final @NonNull List<List<DiscoveryParams>> sections, final @Nullable Category expandedCategory) {

    return Observable.from(sections)
      .map(HamburgerViewModel::massageRows)
      .map(rows -> {
        final Category sectionCategory = rows.get(0).params().category();
        if (sectionCategory != null && expandedCategory != null) {
          return Pair.create(rows, sectionCategory.rootId() == expandedCategory.rootId());
        }
        return Pair.create(rows, false);
      })
      .map(rowsAndIsExpanded ->
        NavigationDrawerData.Section.builder()
          .rows(rowsAndIsExpanded.first)
          .expanded(rowsAndIsExpanded.second)
          .build()
      )
      .toList().toBlocking().single();
  }

  /**
   *
   * @param rows
   * @return
   */
  static List<NavigationDrawerData.Section.Row> massageRows(final @NonNull List<DiscoveryParams> rows) {
    return Observable.from(rows)
      .map(p -> NavigationDrawerData.Section.Row.builder().params(p).build())
      .toList().toBlocking().single();
  }

  /**
   * Determines if a category is visible given what is the currently expanded category.
   * @param category The category to determine its visibility.
   * @param expandedCategory The category that is currently expandable, possible `null`.
   */
  static boolean isVisible(final @NonNull Category category, final @Nullable Category expandedCategory) {
    if (expandedCategory == null) {
      return category.isRoot();
    }

    if (category.isRoot()) {
      return true;
    }

    return category.root().id() == expandedCategory.id();
  }

  /**
   * Since there are two rows that correspond to a root category in an expanded section (e.g. "Art" & "All of Art"),
   * this method will double up that root category in such a situation.
   * @param category The category that might potentially be doubled up.
   * @param expandedCategory The currently expanded category.
   */
  static Observable<Category> doubleRootIfExpanded(final @NonNull Category category, final @Nullable Category expandedCategory) {
    if (expandedCategory == null) {
      return Observable.just(category);
    }

    if (category.isRoot() && category.id() == expandedCategory.id()) {
      return Observable.just(category, category);
    }

    return Observable.just(category);
  }

  /**
   * Returns a list of top-level section filters that can be used based on the current user, which could be `null`.
   * @param user The currently logged in user, can be `null`.
   */
  static Observable<NavigationDrawerData.Section> topSections(final @Nullable User user) {
    List<DiscoveryParams> filters = ListUtils.empty();

    filters.add(DiscoveryParams.builder().staffPicks(true).build());
    if (user != null) {
      filters.add(DiscoveryParams.builder().starred(1).build());
    }
    filters.add(DiscoveryParams.builder().build());

    return Observable.from(filters)
      .map(p -> NavigationDrawerData.Section.Row.builder().params(p).build())
      .map(Collections::singletonList)
      .map(rows -> NavigationDrawerData.Section.builder().rows(rows).build());
  }
}
