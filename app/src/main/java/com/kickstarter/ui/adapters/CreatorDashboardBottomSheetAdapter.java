package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

public final class CreatorDashboardBottomSheetAdapter extends KSAdapter {

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.creator_dashboard_project_switcher_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new CreatorDashboardBottomSheetViewHolder(view);
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    clearSections();
    addSection(projects);
    notifyDataSetChanged();
  }
}
