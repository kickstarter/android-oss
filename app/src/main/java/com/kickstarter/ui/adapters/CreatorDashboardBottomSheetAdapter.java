package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class CreatorDashboardBottomSheetAdapter extends KSAdapter {
  private static final int SECTION_PROJECTS_HEADER = 0;
  private static final int SECTION_PROJECTS = 1;

  private Delegate delegate;

  public interface Delegate extends CreatorDashboardBottomSheetViewHolder.Delegate {}

  public CreatorDashboardBottomSheetAdapter(final @Nullable Delegate delegate) {
    this.delegate = delegate;
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    switch (sectionRow.section()) {
      case SECTION_PROJECTS_HEADER:
        return R.layout.creator_dashboard_project_switcher_header;
      case SECTION_PROJECTS:
        return R.layout.creator_dashboard_project_switcher_view;
    }
    return R.layout.empty_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.creator_dashboard_project_switcher_view:
        return new CreatorDashboardBottomSheetViewHolder(view, this.delegate);
      case R.layout.creator_dashboard_project_switcher_header:
      default:
        return new EmptyViewHolder(view);
    }
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    clearSections();
    insertSection(SECTION_PROJECTS_HEADER, Collections.singletonList(Empty.get()));
    insertSection(SECTION_PROJECTS, projects);
    notifyDataSetChanged();
  }
}
