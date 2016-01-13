package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.Avatar;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class LoggedInMenuProfileViewHolder {
  @Bind(R.id.menu_item_title) TextView menuItemTitleTextView;
  @Bind(R.id.avatar) ImageView avatarImageView;
  @BindString(R.string.accessibility_discovery_buttons_user_profile) String profileButtonString;

  public LoggedInMenuProfileViewHolder(@NonNull final View view, final @NonNull String title, final @NonNull Avatar avatar) {
    ButterKnife.bind(this, view);

    menuItemTitleTextView.setText(title);
    menuItemTitleTextView.setContentDescription(profileButtonString);

    Picasso.with(view.getContext()).load(avatar
      .small())
      .transform(new CircleTransformation())
      .into(avatarImageView);
  }
}
