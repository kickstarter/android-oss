package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.ProjectNotificationViewBinding;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectNotificationViewHolder;

import java.util.List;

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
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    return new ProjectNotificationViewHolder(ProjectNotificationViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
  }

}
