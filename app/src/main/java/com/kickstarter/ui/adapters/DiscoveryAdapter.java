package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import java.util.ArrayList;
import java.util.List;

public final class DiscoveryAdapter extends KSAdapter {
  private static final int DISCOVERY_ONBOARDING_VIEW = 0;
  private static final int DISCOVERY_PROJECT_CARD_VIEW = 1;

  private final Delegate delegate;
  private User user;

  public interface Delegate extends ProjectCardViewHolder.Delegate, DiscoveryOnboardingViewHolder.Delegate {}

  public DiscoveryAdapter(@NonNull final Delegate delegate) {
    this.delegate = delegate;
    this.user = null;
  }

  public void takeUser(final @Nullable User user) {
    this.user = user;
    notifyDataSetChanged();
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    data().clear();
    data().add(DISCOVERY_ONBOARDING_VIEW, new ArrayList<>());
    data().add(projects);
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.row() == DISCOVERY_ONBOARDING_VIEW) {
      if (user == null) { //(&& hasn't seen onboarding yet this session - store pref on create, clear on leave)
        return R.layout.discovery_onboarding_view;
      } else {
        return R.layout.empty_view;
      }
    } else if (sectionRow.row() >= DISCOVERY_PROJECT_CARD_VIEW) {
      return R.layout.project_card_view;
    } else {
      return R.layout.empty_view;
    }
  }

  @Override
  protected KSViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    switch (layout) {
      case R.layout.discovery_onboarding_view:
        return new DiscoveryOnboardingViewHolder(view, delegate);
      case R.layout.project_card_view:
        return new ProjectCardViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
