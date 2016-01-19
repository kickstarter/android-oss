package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.DiscoveryActivityViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import java.util.Collections;
import java.util.List;

public final class DiscoveryAdapter extends KSAdapter {
  private static final int SECTION_ONBOARDING_VIEW = 0;
  private static final int SECTION_ACTIVITY_SAMPLE_VIEW = 1;
  private static final int SECTION_PROJECT_CARD_VIEW = 2;

  private final Delegate delegate;

  public interface Delegate extends ProjectCardViewHolder.Delegate, DiscoveryOnboardingViewHolder.Delegate,
  DiscoveryActivityViewHolder.Delegate {}

  public DiscoveryAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;

    insertSection(SECTION_ONBOARDING_VIEW, Collections.emptyList());
    insertSection(SECTION_ACTIVITY_SAMPLE_VIEW, Collections.emptyList());
    insertSection(SECTION_PROJECT_CARD_VIEW, Collections.emptyList());
  }

  public void setShouldShowOnboardingView(final boolean shouldShowOnboardingView) {
    if (shouldShowOnboardingView) {
      setSection(SECTION_ONBOARDING_VIEW, Collections.singletonList(null));
    } else {
      setSection(SECTION_ONBOARDING_VIEW, Collections.emptyList());
    }
    notifyDataSetChanged();
  }

  public void takeActivity(final @Nullable Activity activity) {
    if (activity == null) {
      setSection(SECTION_ACTIVITY_SAMPLE_VIEW, Collections.emptyList());
    } else {
      setSection(SECTION_ACTIVITY_SAMPLE_VIEW, Collections.singletonList(activity));
    }
    notifyDataSetChanged();
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    setSection(SECTION_PROJECT_CARD_VIEW, projects);
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == SECTION_ONBOARDING_VIEW) {
      return R.layout.discovery_onboarding_view;
    } else if (sectionRow.section() == SECTION_ACTIVITY_SAMPLE_VIEW) {
      return R.layout.discovery_activity_sample_view;
    } else {
      return R.layout.project_card_view;
    }
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.discovery_onboarding_view:
        return new DiscoveryOnboardingViewHolder(view, delegate);
      case R.layout.discovery_activity_sample_view:
        return new DiscoveryActivityViewHolder(view, delegate);
      case R.layout.project_card_view:
        return new ProjectCardViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
