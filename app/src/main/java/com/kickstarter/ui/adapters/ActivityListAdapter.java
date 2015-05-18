package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ViewHolder> {
  private List<Activity> activities;
  private ActivityFeedPresenter presenter;

  public ActivityListAdapter(final List<Activity> activities, final ActivityFeedPresenter presenter) {
    this.activities = activities;
    this.presenter = presenter;
  }

  @Override
  public void onBindViewHolder(final ViewHolder view_holder, final int i) {
    final Activity activity = activities.get(i);
    view_holder.activity = activity;
    view_holder.id.setText(Integer.toString(activity.id()));
  }

  @Override
  public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    View view = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.activity_view, viewGroup, false);

    return new ViewHolder(view, presenter);
  }

  @Override
  public int getItemCount() {
    return activities.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.id) TextView id;
    protected Activity activity;
    protected View view;
    protected Presenter presenter;

    public ViewHolder(final View view, final ActivityFeedPresenter presenter) {
      super(view);

      this.view = view;
      this.presenter = presenter;
      ButterKnife.inject(this, view);
    }
  }
}
