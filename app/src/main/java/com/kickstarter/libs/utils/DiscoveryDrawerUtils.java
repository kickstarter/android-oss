package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.models.Category;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import rx.Observable;

import static com.kickstarter.libs.utils.BoolUtils.isTrue;

public final class DiscoveryDrawerUtils {
  private DiscoveryDrawerUtils() {}

  /**
   * Converts all the disparate data representing the state of the menu data into a `NavigationDrawerData` object
   * that can be used to populate a view.
   *
   * @param categories The full list of categories that can be displayed.
   * @param selected The params that correspond to what is currently selected in the menu.
   * @param expandedCategory The category that correspond to what is currently expanded in the menu.
   * @param user The currently logged in user.
   */
  public static @NonNull NavigationDrawerData deriveNavigationDrawerData(final @NonNull List<Category> categories,
    final @NonNull DiscoveryParams selected, final @Nullable Category expandedCategory, final @Nullable User user) {

    final NavigationDrawerData.Builder builder = NavigationDrawerData.builder();

    List<NavigationDrawerData.Section> categorySections = Observable.from(categories)
      .filter(c -> isVisible(c, expandedCategory))
      .flatMap(c -> doubleRootIfExpanded(c, expandedCategory))
      .map(c -> DiscoveryParams.builder().category(c).build())
      .toList()
      .map(DiscoveryDrawerUtils::paramsGroupedByRootCategory)
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
   *
   * @param sections
   * @return
   */
  private static @NonNull List<NavigationDrawerData.Section> massageSections(final @NonNull List<List<DiscoveryParams>> sections, final @Nullable Category expandedCategory) {

    return Observable.from(sections)
      .map(DiscoveryDrawerUtils::massageRows)
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
  private static @NonNull List<NavigationDrawerData.Section.Row> massageRows(final @NonNull List<DiscoveryParams> rows) {
    return Observable.from(rows)
      .map(p -> NavigationDrawerData.Section.Row.builder().params(p).build())
      .toList().toBlocking().single();
  }

  /**
   * Determines if a category is visible given what is the currently expanded category.
   * @param category The category to determine its visibility.
   * @param expandedCategory The category that is currently expandable, possible `null`.
   */
  private static boolean isVisible(final @NonNull Category category, final @Nullable Category expandedCategory) {
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
  private static @NonNull Observable<Category> doubleRootIfExpanded(final @NonNull Category category, final @Nullable Category expandedCategory) {
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
   * Each filter is its own section containing one single row.
   *
   * @param user The currently logged in user, can be `null`.
   */
  private static @NonNull List<NavigationDrawerData.Section> topSections(final @Nullable User user) {
    List<DiscoveryParams> filters = ListUtils.empty();

    filters.add(DiscoveryParams.builder().staffPicks(true).build());
    if (user != null) {
      filters.add(DiscoveryParams.builder().starred(1).build());
      if (isTrue(user.social())) {
        filters.add(DiscoveryParams.builder().social(1).build());
      }
    }
    filters.add(DiscoveryParams.builder().build());

    return Observable.from(filters)
      .map(p -> NavigationDrawerData.Section.Row.builder().params(p).build())
      .map(Collections::singletonList)
      .map(rows -> NavigationDrawerData.Section.builder().rows(rows).build())
      .toList().toBlocking().single();
  }

  /**
   * Converts the full list of category discovery params into a grouped list of params. A group corresponds to a root
   * category, and the list contains all subcategories.
   */
  private static @NonNull List<List<DiscoveryParams>> paramsGroupedByRootCategory(final @NonNull List<DiscoveryParams> ps) {
    TreeMap<String, List<DiscoveryParams>> grouped = new TreeMap<>();
    for (final DiscoveryParams p : ps) {
      if (!grouped.containsKey(p.category().root().name())) {
        grouped.put(p.category().root().name(), new ArrayList<>());
      }
      grouped.get(p.category().root().name()).add(p);
    }

    return new ArrayList<>(grouped.values());
  }
}
