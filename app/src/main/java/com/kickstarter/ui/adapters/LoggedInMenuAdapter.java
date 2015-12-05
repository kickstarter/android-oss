package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.LoggedInMenuViewHolder;

import java.util.List;

public final class LoggedInMenuAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends LoggedInMenuViewHolder.Delegate {}

  public LoggedInMenuAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeItems(List<Object> menuData) {
    data().clear();
    data().add(menuData);
    notifyDataSetChanged();
  }

  protected @LayoutRes
  int layout(@NonNull final SectionRow sectionRow) {
    return R.layout.logged_in_menu_item;
  }

  protected KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new LoggedInMenuViewHolder(view, delegate);
  }
}
