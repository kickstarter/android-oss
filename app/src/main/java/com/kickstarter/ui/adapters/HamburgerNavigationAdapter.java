package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Empty;
import com.kickstarter.models.HamburgerNavigationData;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedInViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedOutViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;

public final class HamburgerNavigationAdapter extends KSAdapter {
  public HamburgerNavigationAdapter() {}

  // TODO: Replace object type for filters list
  public void data(final @NonNull HamburgerNavigationData data) {
    data().clear();

    // HEADER
    data().add(Collections.singletonList(data.user() != null ? data.user() : Empty.get()));

    // TOP FILTERS (e.g. staff picks, friends backed, ...)
    data().add(data.topFilters());

    // DIVIDER

    // CATEGORY FILTERS

    notifyDataSetChanged();
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    final int section = sectionRow.section();
    final Object object = objectFromSectionRow(sectionRow);
    if (section == 0) {
      return (object instanceof User) ?
        R.layout.hamburger_navigation_header_logged_in_view :
        R.layout.hamburger_navigation_header_logged_out_view;
    } else if (object instanceof DiscoveryParams) {
      return R.layout.hamburger_navigation_filter_view;
    }
    return R.layout.hamburger_divider_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.hamburger_navigation_header_logged_in_view:
        return new HamburgerNavigationHeaderLoggedInViewHolder(view);
      case R.layout.hamburger_navigation_header_logged_out_view:
        return new HamburgerNavigationHeaderLoggedOutViewHolder(view);
      case R.layout.hamburger_navigation_filter_view:
        return new HamburgerNavigationFilterViewHolder(view);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
