package com.kickstarter.ui.view_holders;

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

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FriendFollowViewHolder extends ActivityListViewHolder {
  @InjectView(R.id.avatar) ImageView avatar;
  @InjectView(R.id.title) TextView title;

  public FriendFollowViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    Picasso.with(view.getContext())
      .load(activity.user().avatar().small())
      .transform(new CircleTransform())
      .into(avatar);

    title.setText(view.getResources().getString(R.string.username_is_following_you, activity.user().name()));
    Spannable title_str = (Spannable) title.getText();
    title_str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
      0,
      activity.user().name().length(),
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }
}
