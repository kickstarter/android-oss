package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Notification;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ManageProjectNotificationsViewHolder;

import java.util.List;

public final class ManageProjectNotificationsAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate { }

  public ManageProjectNotificationsAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.manage_project_notification_view;
  }

  public void takeProjects(final @NonNull List<Notification> projectNotifications) {
    data().clear();
    data().add(projectNotifications);
    notifyDataSetChanged();
  }

  @Override
  protected KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new ManageProjectNotificationsViewHolder(view);
  }
}
