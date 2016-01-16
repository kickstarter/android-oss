package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscoveryDrawerAdapter extends KSAdapter {
  private @NonNull Delegate delegate;
  private @NonNull NavigationDrawerData drawerData;

  public DiscoveryDrawerAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
    setHasStableIds(true);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public interface Delegate extends TopFilterViewHolder.Delegate, ParentFilterViewHolder.Delegate, ChildFilterViewHolder.Delegate {}

  @Override
  protected int layout(@NonNull SectionRow sectionRow) {
    final Object object = objectFromSectionRow(sectionRow);
    switch (sectionRow.section()) {
      case 0:
        return (object == null) ?
          R.layout.discovery_drawer_logged_out_view :
          R.layout.discovery_drawer_logged_in_view;
      default:
        return layoutForDatum(object, sectionRow);
    }
  }

  private int layoutForDatum(Object datum, SectionRow sectionRow) {
    if (datum instanceof NavigationDrawerData.Section.Row) {
      NavigationDrawerData.Section.Row row = (NavigationDrawerData.Section.Row) datum;
      if (sectionRow.row() == 0) {
        return row.params().isCategorySet() ?
          R.layout.discovery_drawer_parent_filter_view :
          R.layout.discovery_drawer_top_filter_view;
      } else {
        return R.layout.discovery_drawer_child_filter_view;
      }
    }
    return R.layout.discovery_drawer_divider;
  }

  @Override
  protected Object objectFromSectionRow(@NonNull SectionRow sectionRow) {
    final Object object = super.objectFromSectionRow(sectionRow);

    if (object == null) {
      return null;
    }
    if (object instanceof User) {
      return object;
    }

    final NavigationDrawerData.Section.Row row = (NavigationDrawerData.Section.Row) object;

    final boolean expanded;
    if (row.params().category() == null || drawerData.expandedCategory() == null) {
      expanded = false;
    } else {
      expanded = row.params().category().rootId() == drawerData.expandedCategory().rootId();
    }

    return row
      .toBuilder()
      .selected(row.params().equals(drawerData.selectedParams()))
      .rootIsExpanded(expanded)
      .build();
  }

  @NonNull
  @Override
  protected KSViewHolder viewHolder(@LayoutRes int layout, @NonNull View view) {
    switch (layout) {
      case R.layout.discovery_drawer_logged_in_view:
        return new LoggedInViewHolder(view);
      case R.layout.discovery_drawer_logged_out_view:
        return new LoggedOutViewHolder(view);
      case R.layout.discovery_drawer_parent_filter_view:
        return new ParentFilterViewHolder(view, delegate);
      case R.layout.discovery_drawer_top_filter_view:
        return new TopFilterViewHolder(view, delegate);
      case R.layout.discovery_drawer_child_filter_view:
        return new ChildFilterViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }

  public void takeData(final @NonNull NavigationDrawerData data) {
    drawerData = data;
    this.sections().clear();
    this.sections().addAll(sectionsFromData(data));
    notifyDataSetChanged();
  }

//  public int selectedPosition() {
//    for (final List<NavigationDrawerData.Section.Row> section : sections()) {
//      for (final NavigationDrawerData.Section.Row row : section) {
//
//      }
//    }
//  }

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
