package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClientType;
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

  public void takeNotifications(final @NonNull List<Notification> projectNotifications, final @NonNull ApiClientType client) {
    data().clear();
    for (final Notification notification : projectNotifications) {
      data().add(Collections.singletonList(new ProjectNotificationViewModel(notification, client)));
    }
    notifyDataSetChanged();
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new ProjectNotificationViewHolder(view);
  }
}
