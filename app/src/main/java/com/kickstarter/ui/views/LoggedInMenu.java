package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.models.User;
import com.kickstarter.ui.activities.ProfileActivity;
import com.kickstarter.ui.adapters.LoggedInMenuAdapter;

public class LoggedInMenu extends ListPopupWindow {

  public LoggedInMenu(final @NonNull Context context, final @NonNull User currentUser, final @NonNull View anchorView) {
    super(context);
    setAnchorView(anchorView);
    setModal(true);

    setWidth(context.getResources().getDimensionPixelSize(R.dimen.logged_in_menu_width));
    setHorizontalOffset(context.getResources().getDimensionPixelSize(R.dimen.logged_in_menu_horizontal_offset));
    setVerticalOffset(context.getResources().getDimensionPixelSize(R.dimen.logged_in_menu_vertical_offset));
    //the height of the toolbar in landscape mode is different so this will have to grab from a landscape dimens file
    setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dialog_alert_rounded, null));

    final LoggedInMenuAdapter adapter = new LoggedInMenuAdapter(context, currentUser);
    setAdapter(adapter);

    setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
      dismiss();
      final BaseActivity activity = (BaseActivity) context;
      switch (position) {
        case LoggedInMenuAdapter.TYPE_PROFILE:
          final Intent profileIntent = new Intent(activity, ProfileActivity.class);
          activity.startActivity(profileIntent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
      }
    });
  }
}
