package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Notification;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ManageProjectNotificationsViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_notification_switch) SwitchCompat projectNotificationSwitch;

  public ManageProjectNotificationsViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    final Notification notification = (Notification) datum;
    projectNameTextView.setText(notification.project().name());
    projectNotificationSwitch.setChecked(notification.email() && notification.mobile());
  }
}
