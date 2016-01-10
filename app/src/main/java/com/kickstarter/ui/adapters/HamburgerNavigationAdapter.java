package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Empty;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedInViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedOutViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;
import java.util.List;

public final class HamburgerNavigationAdapter extends KSAdapter {
  public HamburgerNavigationAdapter() {}

  // TODO: Replace object type for filters list
  public void data(final @Nullable User user, final @NonNull List<Object> filters) {
    data().clear();

    // HEADER
    data().add(Collections.singletonList(user != null ? user : Empty.get()));

    // FILTERS
    // data().add(filters);

    notifyDataSetChanged();
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return (objectFromSectionRow(sectionRow) instanceof User) ?
        R.layout.hamburger_navigation_header_logged_in_view :
        R.layout.hamburger_navigation_header_logged_out_view;
    } else {
      return R.layout.reward_card_view;
    }
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.hamburger_navigation_header_logged_in_view:
        return new HamburgerNavigationHeaderLoggedInViewHolder(view);
      case R.layout.hamburger_navigation_header_logged_out_view:
        return new HamburgerNavigationHeaderLoggedOutViewHolder(view);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
