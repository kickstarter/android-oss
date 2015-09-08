package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;

public abstract class ActivityListViewHolder extends RecyclerView.ViewHolder {
  protected Activity activity;
  protected View view;
  protected Presenter presenter;

  public ActivityListViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view);

    this.view = view;
    this.presenter = presenter;
  }

  public void onBind(final Activity activity) {
    this.activity = activity;
  }
}
