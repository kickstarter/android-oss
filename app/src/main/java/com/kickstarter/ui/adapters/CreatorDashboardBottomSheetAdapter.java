package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

public final class CreatorDashboardBottomSheetAdapter extends KSAdapter {

  private Delegate delegate;

  public interface Delegate extends CreatorDashboardBottomSheetViewHolder.Delegate {}

  public CreatorDashboardBottomSheetAdapter(final @Nullable Delegate delegate) {
    this.delegate = delegate;
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.creator_dashboard_project_switcher_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.creator_dashboard_project_switcher_view:
        return new CreatorDashboardBottomSheetViewHolder(view, this.delegate);
      case R.layout.creator_dashboard_project_switcher_title:
      default:
        return new EmptyViewHolder(view);
    }
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    clearSections();
    addSection(projects);
    notifyDataSetChanged();
  }
}
