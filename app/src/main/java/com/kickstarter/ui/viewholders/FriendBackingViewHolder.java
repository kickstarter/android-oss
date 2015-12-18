package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.models.Activity;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class FriendBackingViewHolder extends ActivityListViewHolder {
  @Bind(R.id.avatar) ImageView avatarImageView;
  @Bind(R.id.creator_name) TextView creatorNameTextView;
  @Bind(R.id.project_name) TextView projectNameTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  @Bind(R.id.title) TextView titleTextView;

  private final Delegate delegate;

  public interface Delegate {
    void friendBackingClicked(FriendBackingViewHolder viewHolder, Activity activity);
  }

  public FriendBackingViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
    super.onBind(datum);

    final Context context = view.getContext();

    Picasso.with(context)
      .load(activity.user().avatar().small())
      .transform(new CircleTransformation())
      .into(avatarImageView);
    creatorNameTextView.setText(context.getString(R.string.___by_, activity.project().creator().name()));
    projectNameTextView.setText(activity.project().name());
    Picasso.with(context)
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);
    titleTextView.setText(SocialUtils.friendBackingActivityTitle(context,
      activity.user().name(),
      activity.project().category().rootId()));
  }

  @OnClick(R.id.friend_backing_card_view)
  public void onClick() {
    delegate.friendBackingClicked(this, activity);
  }
}

