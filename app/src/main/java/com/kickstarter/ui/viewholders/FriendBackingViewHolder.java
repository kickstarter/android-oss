package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.CircleTransform;
import com.kickstarter.libs.StringUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendBackingViewHolder extends ActivityListViewHolder {
  @Bind(R.id.avatar) ImageView avatarImageView;
  @Bind(R.id.creator_name) TextView creatorNameTextView;
  @Bind(R.id.project_name) TextView projectNameTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  @Bind(R.id.title) TextView titleTextView;

  public FriendBackingViewHolder(final View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final Object datum) {
    super.onBind(datum);

    final Context context = view.getContext();

    Picasso.with(context)
      .load(activity.user().avatar().small())
      .transform(new CircleTransform())
      .into(avatarImageView);
    creatorNameTextView.setText(context.getString(R.string.by_) + activity.project().creator().name());
    projectNameTextView.setText(activity.project().name());
    Picasso.with(context)
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);
    titleTextView.setText(StringUtils.friendBackingActivityTitle(context,
      activity.user().name(),
      activity.project().category().rootId()));
  }
}

