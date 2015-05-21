package com.kickstarter.ui.view_holders;

import android.view.View;

import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;

import butterknife.ButterKnife;

public class ProjectStateChangedPositiveViewHolder extends ActivityListViewHolder {
  public ProjectStateChangedPositiveViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);
  }
}
