package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.kickstarter.ui.view_holders.ActivityListViewHolder;
import com.kickstarter.ui.view_holders.FriendBackingViewHolder;
import com.kickstarter.ui.view_holders.FriendFollowViewHolder;
import com.kickstarter.ui.view_holders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.view_holders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.view_holders.ProjectUpdateViewHolder;

import java.util.List;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListViewHolder> {
  private List<Activity> activities;
  private ActivityFeedPresenter presenter;

  private static final int VIEW_TYPE_FRIEND_BACKING = 0;
  private static final int VIEW_TYPE_FRIEND_FOLLOW = 1;
  private static final int VIEW_TYPE_PROJECT_STATE_CHANGED = 2;
  private static final int VIEW_TYPE_PROJECT_STATE_CHANGED_POSITIVE = 3;
  private static final int VIEW_TYPE_PROJECT_UPDATE = 4;

  public ActivityListAdapter(final List<Activity> activities, final ActivityFeedPresenter presenter) {
    this.activities = activities;
    this.presenter = presenter;
  }

  @Override
  public int getItemViewType(final int position) {
    final Activity activity = activities.get(position);
    switch(activity.category()) {
      case BACKING:
        return VIEW_TYPE_FRIEND_BACKING;
      case FOLLOW:
        return VIEW_TYPE_FRIEND_FOLLOW;
      case FAILURE:
      case CANCELLATION:
      case SUSPENSION:
      case RESUME:
        return VIEW_TYPE_PROJECT_STATE_CHANGED;
      case LAUNCH:
      case SUCCESS:
        return VIEW_TYPE_PROJECT_STATE_CHANGED_POSITIVE;
      case UPDATE:
        return VIEW_TYPE_PROJECT_UPDATE;
      default:
        throw new RuntimeException("Unhandled view type for activity: " + activity.toString());
    }
  }

  @Override
  public void onBindViewHolder(final ActivityListViewHolder view_holder, final int i) {
    final Activity activity = activities.get(i);
    view_holder.onBind(activity);
  }

  @Override
  public ActivityListViewHolder onCreateViewHolder(final ViewGroup view_group, final int view_type) {
    final LayoutInflater layout_inflater = LayoutInflater.from(view_group.getContext());

    final View view;
    switch (view_type) {
      case VIEW_TYPE_FRIEND_BACKING:
        view = layout_inflater.inflate(R.layout.activity_friend_backing_view, view_group, false);
        return new FriendBackingViewHolder(view, presenter);
      case VIEW_TYPE_FRIEND_FOLLOW:
        view = layout_inflater.inflate(R.layout.activity_friend_follow_view, view_group, false);
        return new FriendFollowViewHolder(view, presenter);
      case VIEW_TYPE_PROJECT_STATE_CHANGED:
        view = layout_inflater.inflate(R.layout.activity_project_state_changed_view, view_group, false);
        return new ProjectStateChangedViewHolder(view, presenter);
      case VIEW_TYPE_PROJECT_STATE_CHANGED_POSITIVE:
        view = layout_inflater.inflate(R.layout.activity_project_state_changed_positive_view, view_group, false);
        return new ProjectStateChangedPositiveViewHolder(view, presenter);
      case VIEW_TYPE_PROJECT_UPDATE:
        view = layout_inflater.inflate(R.layout.activity_project_update_view, view_group, false);
        return new ProjectUpdateViewHolder(view, presenter);
      default:
        throw new RuntimeException("Unhandled view type: " + view_type);
    }
  }

  @Override
  public int getItemCount() {
    return activities.size();
  }
}
