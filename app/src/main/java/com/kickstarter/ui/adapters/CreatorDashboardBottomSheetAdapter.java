package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.databinding.CreatorDashboardProjectSwitcherViewBinding;
import com.kickstarter.databinding.EmptyViewBinding;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;
import java.util.List;

public final class CreatorDashboardBottomSheetAdapter extends KSAdapter {
  private static final int SECTION_PROJECTS_HEADER = 0;
  private static final int SECTION_PROJECTS = 1;

  private final Delegate delegate;

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
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    switch (layout) {
      case R.layout.creator_dashboard_project_switcher_view:
        return new CreatorDashboardBottomSheetViewHolder(CreatorDashboardProjectSwitcherViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false), this.delegate);
      case R.layout.creator_dashboard_project_switcher_header:
      default:
        return new EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    clearSections();
    insertSection(SECTION_PROJECTS_HEADER, Collections.singletonList(Empty.get()));
    insertSection(SECTION_PROJECTS, projects);
    notifyDataSetChanged();
  }
}
