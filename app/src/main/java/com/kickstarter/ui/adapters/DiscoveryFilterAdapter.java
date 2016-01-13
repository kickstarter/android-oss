package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Empty;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.DiscoveryFilterStyle;
import com.kickstarter.ui.viewholders.DiscoveryFilterDividerViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryFilterViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import rx.Observable;

public final class DiscoveryFilterAdapter extends KSAdapter {
  private final Delegate delegate;
  private DiscoveryParams selectedParams;

  public interface Delegate extends DiscoveryFilterViewHolder.Delegate {}

  public DiscoveryFilterAdapter(@NonNull final Delegate delegate, @NonNull final DiscoveryParams selectedParams) {
    this.delegate = delegate;
    this.selectedParams = selectedParams;
  }

  public int scrollPositionForSelectedParams() {
    if (!selectedParams.isCategorySet()) {
      // Don't scroll for top filters
      return 0;
    }

    final Pair<Boolean, Integer> foundAndPosition = Observable.from(sections())
      .scan(new Pair<>(false, 0), (accum, list) -> {
        // For each list, check if there are filters where the category root matches the category
        // root for the selected params.
        final boolean found = !Observable.from(list)
          .ofType(DiscoveryFilterViewHolder.Filter.class)
          .filter(f -> f.params().isCategorySet())
          .takeFirst(f -> selectedParams.category().rootId() == f.params().category().rootId())
          .isEmpty().toBlocking().single();

        // If found, just return the top position for the section, otherwise add to our running total.
        final int position = found ? accum.second : accum.second + list.size();
        return new Pair<>(found, position);
      })
      .takeUntil(pair -> pair.first).last().toBlocking().single();

    // Return one less than the position - lets the user see the preceding filter
    return foundAndPosition.first ? Math.max(0, foundAndPosition.second - 1) : 0;
  }

  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    /*
     * Section 0:         Top filters
     * Section 1:         Category divider
     * Section 2..n - 1:  Category filters
     * Section n:         Padding view
     */
    if (sectionRow.section() == 1) {
      return R.layout.discovery_filter_divider_view;
    } else if (sectionRow.section() == sections().size() - 1) {
      return R.layout.grid_2_height_view;
    }
    return R.layout.discovery_filter_view;
  }

  protected @NonNull KSViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    if (layout == R.layout.discovery_filter_divider_view) {
      return new DiscoveryFilterDividerViewHolder(view);
    } else if (layout == R.layout.grid_2_height_view) {
      return new EmptyViewHolder(view);
    }
    return new DiscoveryFilterViewHolder(view, delegate);
  }

  public void takeCategories(@NonNull final List<Category> initialCategories) {
    sections().clear();

    sections().addAll(paramsSections(initialCategories).toList().toBlocking().single());
    sections().add(1, Collections.singletonList(DiscoveryFilterDividerViewHolder.Divider.builder().light(light()).build()));
    sections().add(Collections.singletonList(Empty.get()));

    notifyDataSetChanged();
  }

  /**
   * Returns an Observable where each item is a list of params/style pairs.
   */
  protected Observable<List<DiscoveryFilterViewHolder.Filter>> paramsSections(@NonNull final List<Category> initialCategories) {
    return categoryFilters(initialCategories)
      .startWith(topFilters());
  }

  /**
   * Params for the top section of filters.
   */
  protected Observable<List<DiscoveryFilterViewHolder.Filter>> topFilters() {
    final DiscoveryFilterStyle style = DiscoveryFilterStyle.builder()
      .light(light())
      .primary(true)
      .selected(false)
      .visible(true)
      .build();

    // TODO: Add social filter
    return Observable.just(DiscoveryParams.builder().staffPicks(true).build(),
      DiscoveryParams.builder().starred(1).build(),
      DiscoveryParams.builder().build())
    .map(p -> DiscoveryFilterViewHolder.Filter.builder().params(p).style(style).build())
    .toList();
  }

  /**
   * Transforms a list of categories into an Observable list of params.
   *
   * Each list of params has a duplicate root category. The duplicate will be used as a nested row under the
   * root downstream, e.g.:
   * Art
   *  - All of Art
   */
  protected Observable<List<DiscoveryFilterViewHolder.Filter>> categoryFilters(@NonNull final List<Category> initialCategories) {
    final Observable<Category> categories = Observable.from(initialCategories);

    final Observable<DiscoveryFilterViewHolder.Filter> filters = primaryCategoryFilters(categories.filter(Category::isRoot))
      .concatWith(secondaryCategoryFilters(categories))
      .toSortedList((f1, f2) -> f1.params().category().compareTo(f2.params().category()))
      .flatMap(Observable::from);

    // RxJava has groupBy. groupBy creates an Observable of GroupedObservables - the Observable doesn't complete
    // until all the GroupedObservables have been subscribed to and completed. It's quite confusing to work with,
    // refactor with caution.
    TreeMap<String, ArrayList<DiscoveryFilterViewHolder.Filter>> groupedFilters = filters.reduce(new TreeMap<String, ArrayList<DiscoveryFilterViewHolder.Filter>>(), (hash, filter) -> {
      final String key = filter.params().category().root().name();
      if (!hash.containsKey(key)) {
        hash.put(key, new ArrayList<DiscoveryFilterViewHolder.Filter>());
      }
      hash.get(key).add(filter);
      return hash;
    }).toBlocking().single();

    return Observable.from(new ArrayList(groupedFilters.values()));
  }

  protected Observable<DiscoveryFilterViewHolder.Filter> primaryCategoryFilters(@NonNull final Observable<Category> rootCategories) {
    final DiscoveryFilterStyle.Builder styleBuilder = DiscoveryFilterStyle.builder()
      .primary(true)
      .showLiveProjectsCount(true)
      .visible(true);

    return rootCategories.map(c -> DiscoveryFilterViewHolder.Filter.builder()
      .params(DiscoveryParams.builder().category(c).build())
      .style(styleBuilder
        .light(light())
        .selected(isRootSelected(c))
        .build())
      .build());
  }

  protected Observable<DiscoveryFilterViewHolder.Filter> secondaryCategoryFilters(@NonNull final Observable<Category> categories) {
    return categories
      .filter(this::isRootSelected)
      .map(c -> DiscoveryFilterViewHolder.Filter.builder()
        .params(DiscoveryParams.builder().category(c).build())
        .style((DiscoveryFilterStyle.builder()
          .light(light())
          .primary(false)
          .selected(isSelected(c))
          .visible(true))
          .build())
        .build());
  }

  protected boolean isSelected(@NonNull final Category category) {
    if (selectedParams.category() == null) {
      return false;
    }

    return selectedParams.category().id() == category.id();
  }

  protected boolean isRootSelected(@NonNull final Category category) {
    if (selectedParams.category() == null) {
      return false;
    }

    return selectedParams.category().rootId() == category.rootId();
  }

  protected boolean light() {
    return DiscoveryUtils.overlayShouldBeLight(selectedParams);
  }
}
