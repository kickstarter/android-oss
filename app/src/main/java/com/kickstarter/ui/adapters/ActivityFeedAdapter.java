package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.FriendFollowViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;
import com.kickstarter.ui.viewholders.SurveyHeaderViewHolder;
import com.kickstarter.ui.viewholders.SurveyViewHolder;

import java.util.Collections;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ActivityFeedAdapter extends KSAdapter {
  private static final int SECTION_LOGGED_IN_EMPTY_VIEW = 0;
  private static final int SECTION_LOGGED_OUT_EMPTY_VIEW = 1;
  private static final int SECTION_SURVEYS_HEADER_VIEW = 2;
  private static final int SECTION_SURVEYS_VIEW = 3;
  private static final int SECTION_ACTIVITIES_VIEW = 4;

  private final @Nullable Delegate delegate;

  public interface Delegate extends FriendBackingViewHolder.Delegate, ProjectStateChangedPositiveViewHolder.Delegate,
    ProjectStateChangedViewHolder.Delegate, ProjectUpdateViewHolder.Delegate, EmptyActivityFeedViewHolder.Delegate {}

  public ActivityFeedAdapter(final @Nullable Delegate delegate) {
    this.delegate = delegate;

    insertSection(SECTION_LOGGED_IN_EMPTY_VIEW, Collections.emptyList());
    insertSection(SECTION_LOGGED_OUT_EMPTY_VIEW, Collections.emptyList());
    insertSection(SECTION_SURVEYS_HEADER_VIEW, Collections.emptyList());
    insertSection(SECTION_SURVEYS_VIEW, Collections.emptyList());
    insertSection(SECTION_ACTIVITIES_VIEW, Collections.emptyList());
  }

  public void takeActivities(final @NonNull List<Activity> activities) {
    setSection(SECTION_ACTIVITIES_VIEW, activities);
    notifyDataSetChanged();
  }

  public void takeSurveys(final @NonNull List<SurveyResponse> surveyResponses) {
    if (surveyResponses.size() > 0) {
      setSection(SECTION_SURVEYS_HEADER_VIEW, Collections.singletonList(surveyResponses.size()));
      setSection(SECTION_SURVEYS_VIEW, surveyResponses);
    } else {
      setSection(SECTION_SURVEYS_HEADER_VIEW, Collections.emptyList());
      setSection(SECTION_SURVEYS_VIEW, Collections.emptyList());
    }
    notifyDataSetChanged();
  }

  public void showLoggedInEmptyState(final boolean show) {
    setSection(SECTION_LOGGED_IN_EMPTY_VIEW, show ? Collections.singletonList(true) : ListUtils.empty());
    notifyDataSetChanged();
  }

  public void showLoggedOutEmptyState(final boolean show) {
    setSection(SECTION_LOGGED_OUT_EMPTY_VIEW, show ? Collections.singletonList(false) : ListUtils.empty());
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    switch (sectionRow.section()) {
      case SECTION_LOGGED_IN_EMPTY_VIEW:
        return R.layout.empty_activity_feed_view;
      case SECTION_LOGGED_OUT_EMPTY_VIEW:
        return R.layout.empty_activity_feed_view;
      case SECTION_SURVEYS_HEADER_VIEW:
        return R.layout.activity_survey_header_view;
      case SECTION_SURVEYS_VIEW:
        return R.layout.activity_survey_view;
      case SECTION_ACTIVITIES_VIEW:
        return getActivityLayoutId(sectionRow);
    }
    return R.layout.empty_view;
  }

  private int getActivityLayoutId(final @NonNull SectionRow sectionRow) {
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
      }
    }
    return R.layout.empty_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.activity_survey_header_view:
        return new SurveyHeaderViewHolder(view);
      case R.layout.activity_survey_view:
        return new SurveyViewHolder(view);
      case R.layout.activity_friend_backing_view:
        return new FriendBackingViewHolder(view, this.delegate);
      case R.layout.activity_friend_follow_view:
        return new FriendFollowViewHolder(view);
      case R.layout.activity_project_state_changed_view:
        return new ProjectStateChangedViewHolder(view, this.delegate);
      case R.layout.activity_project_state_changed_positive_view:
        return new ProjectStateChangedPositiveViewHolder(view, this.delegate);
      case R.layout.activity_project_update_view:
        return new ProjectUpdateViewHolder(view, this.delegate);
      case R.layout.empty_activity_feed_view:
        return new EmptyActivityFeedViewHolder(view, this.delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
