package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.PopupMenu;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.ui.activities.HelpActivity;

public class LoginPopupMenu extends PopupMenu {

  public LoginPopupMenu(@NonNull final Context context, @NonNull final View anchor) {
    super(context, anchor);
    getMenuInflater().inflate(R.menu.login_help_menu, getMenu());
    BaseActivity activity = (BaseActivity) context;

    setOnMenuItemClickListener(item -> {
      final Intent intent;
      switch (item.getItemId()) {
        case R.id.terms:
          intent = new Intent(context, HelpActivity.Terms.class);
          activity.startActivity(intent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
        case R.id.privacy_policy:
          intent = new Intent(context, HelpActivity.Privacy.class);
          activity.startActivity(intent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
        case R.id.cookie_policy:
          intent = new Intent(context, HelpActivity.CookiePolicy.class);
          activity.startActivity(intent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
        case R.id.help:
          intent = new Intent(context, HelpActivity.Faq.class);
          activity.startActivity(intent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
      }
      return true;
    });
  }
}
