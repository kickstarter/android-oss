package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.FriendFollowViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;

import java.util.List;

public class ActivityFeedAdapter extends KsrAdapter {

  public ActivityFeedAdapter(@NonNull final List<Activity> activities) {
    data().add(activities);
  }

  @Override
  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
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
  }

  @Override
  protected KsrViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    switch (layout) {
      case R.layout.activity_friend_backing_view:
        return new FriendBackingViewHolder(view);
      case R.layout.activity_friend_follow_view:
        return new FriendFollowViewHolder(view);
      case R.layout.activity_project_state_changed_view:
        return new ProjectStateChangedViewHolder(view);
      case R.layout.activity_project_state_changed_positive_view:
        return new ProjectStateChangedPositiveViewHolder(view);
      case R.layout.activity_project_update_view:
        return new ProjectUpdateViewHolder(view);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
