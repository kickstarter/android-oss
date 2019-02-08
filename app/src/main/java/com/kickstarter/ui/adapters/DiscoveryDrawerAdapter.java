package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.HeaderViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;

public class DiscoveryDrawerAdapter extends KSAdapter {
  private @NonNull Delegate delegate;
  private @NonNull NavigationDrawerData drawerData;

  public DiscoveryDrawerAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
    setHasStableIds(true);
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  public interface Delegate extends LoggedInViewHolder.Delegate, LoggedOutViewHolder.Delegate,
    TopFilterViewHolder.Delegate, ParentFilterViewHolder.Delegate, ChildFilterViewHolder.Delegate {}

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
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

  private int layoutForDatum(final @NonNull Object datum, final @NonNull SectionRow sectionRow) {
    if (datum instanceof NavigationDrawerData.Section.Row) {
      final NavigationDrawerData.Section.Row row = (NavigationDrawerData.Section.Row) datum;
      if (sectionRow.row() == 0) {
        return row.params().isCategorySet() ?
          R.layout.discovery_drawer_parent_filter_view :
          R.layout.discovery_drawer_top_filter_view;
      } else {
        return R.layout.discovery_drawer_child_filter_view;
      }
    } else if (datum instanceof Integer) {
      return R.layout.discovery_drawer_header;
    }
    return R.layout.horizontal_line_1dp_view;
  }

  @Override
  protected @Nullable Object objectFromSectionRow(final @NonNull SectionRow sectionRow) {
    final Object object = super.objectFromSectionRow(sectionRow);

    if (object == null) {
      return null;
    }
    if (object instanceof User || object instanceof Integer) {
      return object;
    }

    final NavigationDrawerData.Section.Row row = (NavigationDrawerData.Section.Row) object;

    final boolean expanded;
    if (row.params().category() == null || this.drawerData.expandedCategory() == null) {
      expanded = false;
    } else {
      expanded = row.params().category().rootId() == this.drawerData.expandedCategory().rootId();
    }

    return row
      .toBuilder()
      .selected(row.params().equals(this.drawerData.selectedParams()))
      .rootIsExpanded(expanded)
      .build();
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.discovery_drawer_logged_in_view:
        return new LoggedInViewHolder(view, this.delegate);
      case R.layout.discovery_drawer_logged_out_view:
        return new LoggedOutViewHolder(view, this.delegate);
      case R.layout.discovery_drawer_parent_filter_view:
        return new ParentFilterViewHolder(view, this.delegate);
      case R.layout.discovery_drawer_top_filter_view:
        return new TopFilterViewHolder(view, this.delegate);
      case R.layout.discovery_drawer_child_filter_view:
        return new ChildFilterViewHolder(view, this.delegate);
      case R.layout.discovery_drawer_header:
        return new HeaderViewHolder(view);
      case R.layout.discovery_drawer_divider_view:
      default:
        return new EmptyViewHolder(view);
    }
  }

  public void takeData(final @NonNull NavigationDrawerData data) {
    this.drawerData = data;
    this.sections().clear();
    this.sections().addAll(sectionsFromData(data));
    notifyDataSetChanged();
  }

  private @NonNull List<List<Object>> sectionsFromData(final @NonNull NavigationDrawerData data) {
    final List<List<Object>> newSections = new ArrayList<>();

    newSections.add(Collections.singletonList(data.user()));

    newSections.add(Collections.singletonList(null)); // Divider

    newSections.add(Collections.singletonList(R.string.Collections)); // Divider

    final List<NavigationDrawerData.Section> topFilterSections = Observable.from(data.sections())
      .filter(NavigationDrawerData.Section::isTopFilter)
      .toList().toBlocking().single();

    final List<NavigationDrawerData.Section> categoryFilterSections = Observable.from(data.sections())
      .filter(NavigationDrawerData.Section::isCategoryFilter)
      .toList().toBlocking().single();

    for (final NavigationDrawerData.Section section : topFilterSections) {
      newSections.add(new ArrayList<>(section.rows()));
    }

    newSections.add(Collections.singletonList(null)); // Divider

    for (final NavigationDrawerData.Section section : categoryFilterSections) {
      newSections.add(new ArrayList<>(section.rows()));
    }

    return newSections;
  }
}
