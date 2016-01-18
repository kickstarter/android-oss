package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.LoggedInMenuAdapter;

public class LoggedInMenu extends ListPopupWindow {

  public LoggedInMenu(final @NonNull Context context, final @NonNull User currentUser, final @NonNull View anchorView) {
    super(context);
    setAnchorView(anchorView);
    setModal(true);

    final LoggedInMenuAdapter adapter = new LoggedInMenuAdapter(context, currentUser);
    setAdapter(adapter);

    setWidth(context.getResources().getDimensionPixelSize(R.dimen.logged_in_menu_width));
    setHorizontalOffset(context.getResources().getDimensionPixelSize(R.dimen.logged_in_menu_horizontal_offset));
    setVerticalOffset(context.getResources().getDimensionPixelSize(R.dimen.logged_in_menu_vertical_offset));
    setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_alert_rounded));

    setOnItemClickListener((final AdapterView<?> parent, final View view, final int position, final long id) -> {
      dismiss();
      context.startActivity(new Intent(context, adapter.getActivityClassForRow(position)));
    });
  }
}
