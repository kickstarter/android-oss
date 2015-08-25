package com.kickstarter.ui.adapters;

import android.support.annotation.IntDef;
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListViewHolder> {
  private List<Activity> activities;
  private ActivityFeedPresenter presenter;

  private static final int FRIEND_BACKING = 0;
  private static final int FRIEND_FOLLOW = 1;
  private static final int PROJECT_STATE_CHANGED = 2;
  private static final int PROJECT_STATE_CHANGED_POSITIVE = 3;
  private static final int PROJECT_UPDATE = 4;

  @IntDef({FRIEND_BACKING, FRIEND_FOLLOW, PROJECT_STATE_CHANGED, PROJECT_STATE_CHANGED_POSITIVE, PROJECT_UPDATE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ViewType {}

  public ActivityListAdapter(final List<Activity> activities, final ActivityFeedPresenter presenter) {
    this.activities = activities;
    this.presenter = presenter;
  }

  @Override
  public @ViewType int getItemViewType(final int position) {
    final Activity activity = activities.get(position);
    switch(activity.category()) {
      case BACKING:
        return FRIEND_BACKING;
      case FOLLOW:
        return FRIEND_FOLLOW;
      case FAILURE:
      case CANCELLATION:
      case SUSPENSION:
      case RESUME:
        return PROJECT_STATE_CHANGED;
      case LAUNCH:
      case SUCCESS:
        return PROJECT_STATE_CHANGED_POSITIVE;
      case UPDATE:
        return PROJECT_UPDATE;
      default:
        throw new RuntimeException("Unhandled view type for activity: " + activity.toString());
    }
  }

  @Override
  public void onBindViewHolder(final ActivityListViewHolder viewHolder, final int i) {
    final Activity activity = activities.get(i);
    viewHolder.onBind(activity);
  }

  @Override
  public ActivityListViewHolder onCreateViewHolder(final ViewGroup viewGroup, final @ViewType int viewType) {
    final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());

    final View view;
    switch (viewType) {
      case FRIEND_BACKING:
        view = layoutInflater.inflate(R.layout.activity_friend_backing_view, viewGroup, false);
        return new FriendBackingViewHolder(view, presenter);
      case FRIEND_FOLLOW:
        view = layoutInflater.inflate(R.layout.activity_friend_follow_view, viewGroup, false);
        return new FriendFollowViewHolder(view, presenter);
      case PROJECT_STATE_CHANGED:
        view = layoutInflater.inflate(R.layout.activity_project_state_changed_view, viewGroup, false);
        return new ProjectStateChangedViewHolder(view, presenter);
      case PROJECT_STATE_CHANGED_POSITIVE:
        view = layoutInflater.inflate(R.layout.activity_project_state_changed_positive_view, viewGroup, false);
        return new ProjectStateChangedPositiveViewHolder(view, presenter);
      case PROJECT_UPDATE:
        view = layoutInflater.inflate(R.layout.activity_project_update_view, viewGroup, false);
        return new ProjectUpdateViewHolder(view, presenter);
      default:
        throw new RuntimeException("Unhandled view type: " + viewType);
    }
  }

  @Override
  public int getItemCount() {
    return activities.size();
  }
}
