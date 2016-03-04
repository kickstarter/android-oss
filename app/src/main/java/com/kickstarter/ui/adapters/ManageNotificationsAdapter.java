package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Notification;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectNotificationViewHolder;
import com.kickstarter.viewmodels.ProjectNotificationViewModel;

import java.util.Collections;
import java.util.List;

public final class ManageNotificationsAdapter extends KSAdapter {

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.manage_notification_view;
  }

  public void takeNotifications(final @NonNull List<Notification> projectNotifications, final @NonNull Environment environment) {
    sections().clear();
    for (final Notification notification : projectNotifications) {
      sections().add(Collections.singletonList(new ProjectNotificationViewModel(notification, environment)));
    }
    notifyDataSetChanged();
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new ProjectNotificationViewHolder(view);
  }
}
