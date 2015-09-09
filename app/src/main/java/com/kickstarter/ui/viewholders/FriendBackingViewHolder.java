package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.CircleTransform;
import com.kickstarter.libs.StringUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendBackingViewHolder extends ActivityListViewHolder {
  @Bind(R.id.avatar) ImageView avatarImageView;
  @Bind(R.id.creator_name) TextView creatorNameTextView;
  @Bind(R.id.project_name) TextView projectNameTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  @Bind(R.id.title) TextView titleTextView;

  public FriendBackingViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    Picasso.with(view.getContext())
      .load(activity.user().avatar().small())
      .transform(new CircleTransform())
      .into(avatarImageView);
    creatorNameTextView.setText(view.getResources().getString(R.string.by_) + activity.project().creator().name());
    projectNameTextView.setText(activity.project().name());
    Picasso.with(view.getContext())
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);
    titleTextView.setText(StringUtils.friendBackingActivityTitle(view.getContext(),
      activity.user().name(),
      activity.project().category().rootId()));
  }
}

