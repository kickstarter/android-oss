package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.kickstarter.R;
import com.kickstarter.models.Notification;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ManageProjectNotificationsViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_notification_switch) SwitchCompat projectNotificationSwitch;

  private final Delegate delegate;
  private Notification notification;

  public interface Delegate {
    void switchClicked(ManageProjectNotificationsViewHolder viewHolder, Notification notification, boolean toggleValue);
  }

  public ManageProjectNotificationsViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);

    RxCompoundButton.checkedChanges(projectNotificationSwitch)
      .skip(1)
      .subscribe(toggleValue -> delegate.switchClicked(this, notification, toggleValue));
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    notification = (Notification) datum;
    projectNameTextView.setText(notification.project().name());
    projectNotificationSwitch.setChecked(notification.email() && notification.mobile());
  }
}
