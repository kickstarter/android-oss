package com.kickstarter.ui.view_holders;

import android.view.View;

import com.kickstarter.presenters.ActivityFeedPresenter;

import butterknife.ButterKnife;

public class ProjectUpdateViewHolder extends ActivityListViewHolder {
  public ProjectUpdateViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }
}
