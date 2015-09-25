package com.kickstarter.ui.adapters;

import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.DiscoveryFilterStyle;
import com.kickstarter.ui.viewholders.DiscoveryFilterViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public class DiscoveryFilterAdapter extends KsrAdapter {
  private final Delegate delegate;
  private DiscoveryParams selectedDiscoveryParams;

  public interface Delegate extends DiscoveryFilterViewHolder.Delegate {}

  public DiscoveryFilterAdapter(final Delegate delegate, final DiscoveryParams selectedDiscoveryParams) {
    this.delegate = delegate;
    this.selectedDiscoveryParams = selectedDiscoveryParams;
  }

  protected int layout(final SectionRow sectionRow) {
    if (sectionRow.section() == 1) {
      return R.layout.discovery_filter_divider_view;
    }
    return R.layout.discovery_filter_view;
  }

  protected KsrViewHolder viewHolder(final int layout, final View view) {
    if (layout == R.layout.discovery_filter_divider_view) {
      return new EmptyViewHolder(view); // TODO: Might need to make a view holder here that toggles white or dark text
    }
    return new DiscoveryFilterViewHolder(view, delegate);
  }

  public void takeCategories(final List<Category> initialCategories) {
    data().clear();

    data().addAll(discoveryParamsSections(initialCategories).toList().toBlocking().single());
    data().add(1, Collections.singletonList(null)); // Category divider

    notifyDataSetChanged();
  }

  /**
   * Returns an Observable where each item is a list of params/style pairs.
   */
  protected Observable<List<Pair<DiscoveryParams, DiscoveryFilterStyle>>> discoveryParamsSections(final List<Category> initialCategories) {
    return categoryDiscoveryParams(initialCategories)
      .startWith(filterDiscoveryParams())
      .map(paramsList -> {
        return Observable.from(paramsList)
          .map(p -> Pair.create(p, new DiscoveryFilterStyle.Builder().build())).toList().toBlocking().single();
      });
  }

  /**
   * Params for the top section of filters.
   */
  protected Observable<List<DiscoveryParams>> filterDiscoveryParams() {
    // TODO: Add social filter
    return Observable.just(
      new DiscoveryParams.Builder().staffPicks(true).build(),
      new DiscoveryParams.Builder().starred(1).build(),
      new DiscoveryParams.Builder().build() // Everything filter
    ).toList();
  }

  /**
   * Transforms a list of categories into an Observable list of params.
   *
   * Each list of params has a duplicate root category. The duplicate will be used as a nested row under the
   * root downstream, e.g.:
   * Art
   *  - All of Art
   */
  protected Observable<List<DiscoveryParams>> categoryDiscoveryParams(final List<Category> initialCategories) {
    final Observable<Category> categories = Observable.from(initialCategories);

    final Observable<DiscoveryParams> params = categories
      .concatWith(categories.filter(Category::isRoot)) // Add the duplicate root category
      .map(c -> new DiscoveryParams.Builder().category(c).build())
      .toSortedList((p1, p2) -> p1.category().discoveryFilterCompareTo(p2.category()))
      .flatMap(Observable::from);

    return params.groupBy(p -> p.category().rootId())
      .map(Observable::toList)
      .flatMap(l -> l);
  }
}
