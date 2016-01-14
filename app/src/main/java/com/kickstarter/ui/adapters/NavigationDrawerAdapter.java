package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationChildFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedInViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedOutViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationRootFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationTopFilterViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NavigationDrawerAdapter extends KSAdapter {

  private @NonNull Delegate delegate;

  public NavigationDrawerAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
    setHasStableIds(true);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public interface Delegate extends HamburgerNavigationTopFilterViewHolder.Delegate,
    HamburgerNavigationRootFilterViewHolder.Delegate, HamburgerNavigationChildFilterViewHolder.Delegate {}

  @Override
  protected int layout(@NonNull SectionRow sectionRow) {
    final Object object = objectFromSectionRow(sectionRow);
    switch (sectionRow.section()) {
      case 0:
        return (object == null) ?
          R.layout.hamburger_navigation_header_logged_out_view :
          R.layout.hamburger_navigation_header_logged_in_view;
      default:
        return layoutForDatum(object, sectionRow);
    }
  }

  private int layoutForDatum(Object datum, SectionRow sectionRow) {
    if (datum instanceof NavigationDrawerData.Section.Row) {
      NavigationDrawerData.Section.Row row = (NavigationDrawerData.Section.Row) datum;
      if (sectionRow.row() == 0) {
        return row.params().isCategorySet() ?
          R.layout.hamburger_navigation_root_filter_view :
          R.layout.hamburger_navigation_top_filter_view;
      } else {
        return R.layout.hamburger_navigation_child_filter_view;
      }
    }
    return R.layout.hamburger_divider_view;
  }



  @NonNull
  @Override
  protected KSViewHolder viewHolder(@LayoutRes int layout, @NonNull View view) {
    switch (layout) {
      case R.layout.hamburger_navigation_header_logged_in_view:
        return new HamburgerNavigationHeaderLoggedInViewHolder(view);
      case R.layout.hamburger_navigation_header_logged_out_view:
        return new HamburgerNavigationHeaderLoggedOutViewHolder(view);
      case R.layout.hamburger_navigation_root_filter_view:
        return new HamburgerNavigationRootFilterViewHolder(view, delegate);
      case R.layout.hamburger_navigation_top_filter_view:
        return new HamburgerNavigationTopFilterViewHolder(view, delegate);
      case R.layout.hamburger_navigation_child_filter_view:
        return new HamburgerNavigationChildFilterViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }

  public void initialize(final @NonNull NavigationDrawerData data) {

    final List<List<Object>> newSections = sectionsFromData(data);

//    final DiffUtils.Diff diff = DiffUtils.diff(
//      ListUtils.flatten(sections()),
//      ListUtils.flatten(newSections)
//    );

    this.sections().clear();
    this.sections().addAll(newSections);

//    List<Range> deleteRanges = RangeUtils.positionalRanges(diff.deletions());
//    for (final Range range : deleteRanges) {
//      notifyItemRangeRemoved(range.start, range.length);
//    }
//
//    List<Range> insertRanges = RangeUtils.positionalRanges(diff.insertions());
//    for (final Range range : insertRanges) {
//      notifyItemRangeInserted(range.start, range.length);
//    }
//
//    Timber.d("");

//    // no animation
    notifyDataSetChanged();
  }

  List<List<Object>> sectionsFromData(NavigationDrawerData data) {
    final List<List<Object>> newSections = new ArrayList<>();

    newSections.add(Collections.singletonList(data.user()));

    for (final NavigationDrawerData.Section section : data.sections()) {
      newSections.add(new ArrayList<>(section.rows()));
    }

    return newSections;
  }

  private static boolean weakEquality(final @Nullable Object lhs, final @Nullable Object rhs) {

    if ((lhs == null && rhs != null) || (lhs != null && rhs == null)) {
      return false;
    }
    if (lhs == null && rhs == null) {
      return true;
    }
    if (!lhs.getClass().equals(rhs.getClass())) {
      return false;
    }

    if (lhs instanceof User && rhs instanceof User) {
      return lhs.equals(rhs);
    }

    if (lhs instanceof NavigationDrawerData.Section.Row && rhs instanceof NavigationDrawerData.Section.Row) {
      final NavigationDrawerData.Section.Row lhsRow = (NavigationDrawerData.Section.Row) lhs;
      final NavigationDrawerData.Section.Row rhsRow = (NavigationDrawerData.Section.Row) rhs;

      return lhsRow.params().equals(rhsRow.params());
    }

    return false;
  }
}
