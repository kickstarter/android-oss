package com.kickstarter.ui.view_holders;

import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FriendBackingViewHolder extends ActivityListViewHolder {
  @InjectView(R.id.project_name) TextView project_name;

  public FriendBackingViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);
    project_name.setText(activity.category().toString());
  }
}

