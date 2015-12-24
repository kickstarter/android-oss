package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.activities.HelpActivity;
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
    setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dialog_alert_rounded, null));

    setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
      dismiss();

      final Intent intent = new Intent(context, adapter.getActivityClassForRow(position));
      if (position == 1) {
        intent.putExtra(context.getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_FAQ);
      }
      context.startActivity(intent);

//      switch (position) {
//        case LoggedInMenuAdapter.ROW_PROFILE:/**/
//          break;
//        case LoggedInMenuAdapter.ROW_SETTINGS:
//          final Intent settingsIntent = new Intent(activity, SettingsActivity.class);
//          activity.startActivity(settingsIntent);
//          break;
//        case LoggedInMenuAdapter.ROW_HELP:
//          final Intent helpIntent = new Intent(activity, HelpActivity.class);
//          helpIntent.putExtra(context.getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_FAQ);
//          activity.startActivity(helpIntent);
//          break;
//      }
    });
  }
}
