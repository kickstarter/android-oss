package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListPopupWindow;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.LoggedInMenuAdapter;
import com.kickstarter.ui.viewholders.LoggedInMenuProfileViewHolder;
import com.kickstarter.ui.viewholders.LoggedInMenuViewHolder;

import java.util.ArrayList;
import java.util.List;

public class LoggedInMenu extends ListPopupWindow implements LoggedInMenuAdapter.Delegate {
  private LoggedInMenuAdapter adapter;

  public LoggedInMenu(final @NonNull Context context, final @NonNull User currentUser, final @NonNull View anchorView) {
    super(context);
    setAnchorView(anchorView);
    setModal(true);

    setWidth(300);
    setHeight(300);

    adapter = new LoggedInMenuAdapter(this);
    List<String> titles = new ArrayList<String>();
    titles.add(currentUser.name());
    titles.add(context.getResources().getString(R.string.___Find_friends));
    titles.add(context.getResources().getString(R.string.___Settings));
    titles.add(context.getResources().getString(R.string.___Help));

    adapter.takeTitles(titles);
    //this.setAdapter(adapter);
  }

  public void menuItemClicked(LoggedInMenuViewHolder viewHolder, String title) {
    //link out
  }

  public void menuItemClicked(LoggedInMenuProfileViewHolder viewHolder, String title) {
    //link out
  }
}
