package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectNotificationViewHolder;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public final class ProjectNotificationSettingsAdapter extends KSAdapter {
  /**
   * Binds project notifications to the adapter.
   */
  public void projectNotifications(final @NonNull List<ProjectNotification> projectNotifications) {
    clearSections();
    addSection(projectNotifications);
    notifyDataSetChanged();
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.project_notification_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new ProjectNotificationViewHolder(view);
  }
}
