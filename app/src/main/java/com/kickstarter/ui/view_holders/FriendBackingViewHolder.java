package com.kickstarter.ui.view_holders;

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

public class FriendBackingViewHolder extends ActivityListViewHolder {
  @InjectView(R.id.creator_name) TextView creator_name;
  @InjectView(R.id.project_name) TextView project_name;
  @InjectView(R.id.avatar) ImageView avatar;

  public FriendBackingViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    creator_name.setText(view.getResources().getString(R.string.by_) + activity.project().creator().name());
    project_name.setText(activity.project().name());
    Picasso.with(view.getContext())
      .load(activity.user().avatar().small())
      .transform(new CircleTransform())
      .into(avatar);
  }
}

