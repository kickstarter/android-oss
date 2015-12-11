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

    setWidth(400);
    setHeight(500);

    final LoggedInMenuAdapter adapter = new LoggedInMenuAdapter(context);
    adapter.takeTitle(currentUser.name());
    adapter.takeTitle(context.getResources().getString(R.string.___Find_friends));
    adapter.takeTitle(context.getResources().getString(R.string.___Settings));
    adapter.takeTitle(context.getResources().getString(R.string.___Help));
    adapter.takeTitle(context.getResources().getString(R.string.___Log_out));
    setAdapter(adapter);

    setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
      dismiss();
      final BaseActivity activity = (BaseActivity) context;
      switch (position) {
        case LoggedInMenuAdapter.TYPE_PROFILE:
          final Intent profileIntent = new Intent(activity, ProfileActivity.class);
          activity.startActivity(profileIntent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          //          dismiss();
          break;
        //        case LoggedInMenuAdapter.TYPE_LOGOUT:
        //          logout.execute();
        //          final Intent intent = new Intent(activity, DiscoveryActivity.class)
        //            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //          activity.startActivity(intent);
        //          break;
      }
    });
  }
}
