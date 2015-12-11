package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoggedInMenuProfileViewHolder {
  @Bind(R.id.menu_item_title) TextView menuItemTitleTextView;
  @Bind(R.id.avatar) ImageView avatarImageView;

  public LoggedInMenuProfileViewHolder(@NonNull final View view) {
    ButterKnife.bind(this, view);
  }

  public void setTitle(final @NonNull String title) {
    menuItemTitleTextView.setText(title);
  }
}
