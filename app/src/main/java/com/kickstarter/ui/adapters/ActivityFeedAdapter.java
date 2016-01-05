package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Empty;
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

public final class ActivityFeedAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends FriendBackingViewHolder.Delegate, ProjectStateChangedPositiveViewHolder.Delegate,
    ProjectStateChangedViewHolder.Delegate, ProjectUpdateViewHolder.Delegate, EmptyActivityFeedViewHolder.Delegate {}

  public ActivityFeedAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeActivities(final @NonNull List<Activity> activities) {
    data().clear();
    if (activities.size() == 0) {
      data().add(Collections.singletonList(Empty.get()));
    } else {
      data().add(activities);
    }
    notifyDataSetChanged();
  }

  public void takeLoggedOutEmptyState() {
    data().clear();
    data().add(Collections.singletonList(null));
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
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
      return R.layout.empty_activity_feed_view;
    }
  }

  @Override
  protected KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
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
      case R.layout.empty_activity_feed_view:
        return new EmptyActivityFeedViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
