package com.kickstarter.ui.view_holders;

import android.view.View;

import com.kickstarter.presenters.ActivityFeedPresenter;

import butterknife.ButterKnife;

public class ProjectStateChangedViewHolder extends ActivityListViewHolder {
  public ProjectStateChangedViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }
}
