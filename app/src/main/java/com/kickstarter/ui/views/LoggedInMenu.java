package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListPopupWindow;

import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.LoggedInMenuAdapter;
import com.kickstarter.ui.viewholders.LoggedInMenuViewHolder;

public class LoggedInMenu extends ListPopupWindow implements LoggedInMenuAdapter.Delegate {
  private LoggedInMenuAdapter adapter;

  public LoggedInMenu(final @NonNull Context context, final @NonNull User currentUser, final @NonNull View anchorView) {
    super(context);
    setAnchorView(anchorView);
    setModal(true);

    adapter = new LoggedInMenuAdapter(this);
    //adapter.takeItems();
  }

  public void menuItemClicked(LoggedInMenuViewHolder viewHolder, String title) {
    //link out
  }
}
