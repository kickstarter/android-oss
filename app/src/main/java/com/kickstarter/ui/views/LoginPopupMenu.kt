package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.PopupMenu;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.ui.activities.HelpActivity;

import androidx.annotation.NonNull;

public class LoginPopupMenu extends PopupMenu {

  public LoginPopupMenu(final @NonNull Context context, final @NonNull View anchor) {
    super(context, anchor);
    getMenuInflater().inflate(R.menu.login_help_menu, getMenu());
    final BaseActivity activity = (BaseActivity) context;

    setOnMenuItemClickListener(item -> {
      final Intent intent;
      switch (item.getItemId()) {
        case R.id.terms:
          intent = new Intent(context, HelpActivity.Terms.class);
          activity.startActivity(intent);
          break;
        case R.id.privacy_policy:
          intent = new Intent(context, HelpActivity.Privacy.class);
          activity.startActivity(intent);
          break;
        case R.id.cookie_policy:
          intent = new Intent(context, HelpActivity.CookiePolicy.class);
          activity.startActivity(intent);
          break;
        case R.id.help:
          intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT));
          activity.startActivity(intent);
          break;
      }
      return true;
    });
  }
}
