package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Empty;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.FriendFollowViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;

import java.util.Collections;
import java.util.List;

public class ActivityFeedAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends FriendBackingViewHolder.Delegate, ProjectStateChangedPositiveViewHolder.Delegate,
    ProjectStateChangedViewHolder.Delegate, ProjectUpdateViewHolder.Delegate, EmptyActivityFeedViewHolder.Delegate {}

  public ActivityFeedAdapter(@NonNull final Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeActivities(@NonNull final List<Activity> activities) {
    data().clear();
    if (activities.size() == 0) {
      data().add(Collections.singletonList(Empty.create()));
    } else {
      data().add(activities);
    }
    notifyDataSetChanged();
  }

  public void takeEmptyFeed(@Nullable final User user) {
    data().clear();
    data().add(Collections.singletonList(user));
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    if (objectFromSectionRow(sectionRow) instanceof Activity) {
      final Activity activity = (Activity) objectFromSectionRow(sectionRow);
      switch (activity.category()) {
        case Activity.CATEGORY_BACKING:
          return R.layout.activity_friend_backing_view;
        case Activity.CATEGORY_FOLLOW:
          return R.layout.activity_friend_follow_view;
        case Activity.CATEGORY_FAILURE:
        case Activity.CATEGORY_CANCELLATION:
        case Activity.CATEGORY_SUSPENSION:
        case Activity.CATEGORY_RESUME:
          return R.layout.activity_project_state_changed_view;
        case Activity.CATEGORY_LAUNCH:
        case Activity.CATEGORY_SUCCESS:
          return R.layout.activity_project_state_changed_positive_view;
        case Activity.CATEGORY_UPDATE:
          return R.layout.activity_project_update_view;
        default:
          return R.layout.empty_view;
      }
    } else {
      return R.layout.empty_activity_feed_layout;
    }
  }

  @Override
  protected KSViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    switch (layout) {
      case R.layout.activity_friend_backing_view:
        return new FriendBackingViewHolder(view, delegate);
      case R.layout.activity_friend_follow_view:
        return new FriendFollowViewHolder(view);
      case R.layout.activity_project_state_changed_view:
        return new ProjectStateChangedViewHolder(view, delegate);
      case R.layout.activity_project_state_changed_positive_view:
        return new ProjectStateChangedPositiveViewHolder(view, delegate);
      case R.layout.activity_project_update_view:
        return new ProjectUpdateViewHolder(view, delegate);
      case R.layout.empty_activity_feed_layout:
        return new EmptyActivityFeedViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
