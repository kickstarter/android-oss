package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProjectSocialViewHolder extends KSViewHolder {
  protected @Bind(R.id.friend_image) ImageView friendImageView;
  protected @Bind(R.id.friend_name) TextView friendNameTextView;

  public ProjectSocialViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    final User user = (User) datum;
    Picasso.with(view.getContext()).load(user
      .avatar()
      .small())
    .transform(new CircleTransformation())
    .into(friendImageView);

    friendNameTextView.setText(user.name());
  }
}
