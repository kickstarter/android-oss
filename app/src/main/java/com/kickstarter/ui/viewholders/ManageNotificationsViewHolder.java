package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.kickstarter.R;
import com.kickstarter.models.Notification;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ManageNotificationsViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.notification_switch) SwitchCompat notificationSwitch;

  private Notification notification;

  public interface Delegate {
    void switchClicked(ManageNotificationsViewHolder viewHolder, Notification notification, boolean toggleValue);
  }

  public ManageNotificationsViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    ButterKnife.bind(this, view);

    RxCompoundButton.checkedChanges(notificationSwitch)
      .skip(1)
      .subscribe(toggleValue -> {
        delegate.switchClicked(this, notification, toggleValue);
      });
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    notification = (Notification) datum;

    Log.d("TEST", notification.project().name() + " " + notification.mobile());

    projectNameTextView.setText(notification.project().name());
    notificationSwitch.setChecked(notification.email() && notification.mobile());
  }
}
