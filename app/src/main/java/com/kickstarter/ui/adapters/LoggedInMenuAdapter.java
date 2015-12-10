package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.LoggedInMenuProfileViewHolder;
import com.kickstarter.ui.viewholders.LoggedInMenuViewHolder;

import java.util.List;

public final class LoggedInMenuAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends LoggedInMenuViewHolder.Delegate, LoggedInMenuProfileViewHolder.Delegate {}

  public LoggedInMenuAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeTitles(@NonNull final List<String> titles) {
    data().clear();
    data().add(titles);
    notifyDataSetChanged();
  }

  protected @LayoutRes
  int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.row() == 0) {
      return R.layout.logged_in_menu_avatar_item;
    } else {
      return R.layout.logged_in_menu_item;
    }
  }

  protected KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch(layout) {
      case R.layout.logged_in_menu_avatar_item:
        return new LoggedInMenuProfileViewHolder(view, delegate);
      case R.layout.logged_in_menu_item:
      default:
        return new LoggedInMenuViewHolder(view, delegate);
    }
  }
}
