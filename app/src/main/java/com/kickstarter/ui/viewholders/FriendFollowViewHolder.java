package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.CircleTransform;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendFollowViewHolder extends ActivityListViewHolder {
  @Bind(R.id.avatar) ImageView avatarImageView;
  @Bind(R.id.title) TextView titleTextView;

  public FriendFollowViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    final Context context = view.getContext();

    Picasso.with(context)
      .load(activity.user().avatar().small())
      .transform(new CircleTransform())
      .into(avatarImageView);

    titleTextView.setText(context.getString(R.string.username_is_following_you, activity.user().name()));
    final Spannable titleStr = (Spannable) titleTextView.getText();
    titleStr.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
      0,
      activity.user().name().length(),
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }
}
