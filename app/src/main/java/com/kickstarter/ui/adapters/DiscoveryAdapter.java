package com.kickstarter.ui.adapters;

import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.data.Editorial;
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.EditorialViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import java.util.Collections;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DiscoveryAdapter extends KSAdapter {
  private static final int SECTION_ONBOARDING_VIEW = 0;
  private static final int SECTION_EDITORIAL_VIEW = 1;
  private static final int SECTION_ACTIVITY_SAMPLE_VIEW = 2;
  private static final int SECTION_PROJECT_CARD_VIEW = 3;

  private final Delegate delegate;

  public interface Delegate extends DiscoveryOnboardingViewHolder.Delegate,
    EditorialViewHolder.Delegate,
    ActivitySampleFriendFollowViewHolder.Delegate,
    ActivitySampleFriendBackingViewHolder.Delegate,
    ActivitySampleProjectViewHolder.Delegate,
    ProjectCardViewHolder.Delegate {}

  public DiscoveryAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;

    insertSection(SECTION_ONBOARDING_VIEW, Collections.emptyList());
    insertSection(SECTION_EDITORIAL_VIEW, Collections.emptyList());
    insertSection(SECTION_ACTIVITY_SAMPLE_VIEW, Collections.emptyList());
    insertSection(SECTION_PROJECT_CARD_VIEW, Collections.emptyList());
  }

  public void takeActivity(final @Nullable Activity activity) {
    if (activity == null) {
      setSection(SECTION_ACTIVITY_SAMPLE_VIEW, Collections.emptyList());
    } else {
      setSection(SECTION_ACTIVITY_SAMPLE_VIEW, Collections.singletonList(activity));
    }
    notifyDataSetChanged();
  }

  public void setShouldShowEditorial(final @Nullable Editorial editorial) {
    if (editorial == null) {
      setSection(SECTION_EDITORIAL_VIEW, Collections.emptyList());
    } else {
      setSection(SECTION_EDITORIAL_VIEW, Collections.singletonList(editorial));
    }

    notifyDataSetChanged();
  }

  public void setShouldShowOnboardingView(final boolean shouldShowOnboardingView) {
    if (shouldShowOnboardingView) {
      setSection(SECTION_ONBOARDING_VIEW, Collections.singletonList(null));
    } else {
      setSection(SECTION_ONBOARDING_VIEW, Collections.emptyList());
    }

    notifyDataSetChanged();
  }

  public void takeProjects(final @NonNull List<Pair<Project, DiscoveryParams>> projects) {
    setSection(SECTION_PROJECT_CARD_VIEW, projects);
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == SECTION_ONBOARDING_VIEW) {
      return R.layout.discovery_onboarding_view;
    } else if (sectionRow.section() == SECTION_EDITORIAL_VIEW) {
      return R.layout.item_lights_on;
    } else if (sectionRow.section() == SECTION_ACTIVITY_SAMPLE_VIEW) {
      if (objectFromSectionRow(sectionRow) instanceof Activity) {
        final Activity activity = (Activity) objectFromSectionRow(sectionRow);
        if (activity.category().equals(Activity.CATEGORY_BACKING)) {
          return R.layout.activity_sample_friend_backing_view;
        } else if (activity.category().equals(Activity.CATEGORY_FOLLOW)) {
          return R.layout.activity_sample_friend_follow_view;
        } else {
          return R.layout.activity_sample_project_view;
        }
      }
      return R.layout.empty_view;
    } else {
      return R.layout.project_card_view;
    }
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.discovery_onboarding_view:
        return new DiscoveryOnboardingViewHolder(view, this.delegate);
      case R.layout.item_lights_on:
        return new EditorialViewHolder(view, this.delegate);
      case R.layout.project_card_view:
        return new ProjectCardViewHolder(view, this.delegate);
      case R.layout.activity_sample_friend_backing_view:
        return new ActivitySampleFriendBackingViewHolder(view, this.delegate);
      case R.layout.activity_sample_friend_follow_view:
        return new ActivitySampleFriendFollowViewHolder(view, this.delegate);
      case R.layout.activity_sample_project_view:
        return new ActivitySampleProjectViewHolder(view, this.delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
