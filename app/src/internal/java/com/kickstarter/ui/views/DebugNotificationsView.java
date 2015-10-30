package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Notifications;
import com.kickstarter.models.Activity;
import com.kickstarter.models.pushdata.ActivityPushData;
import com.kickstarter.models.pushdata.GCMPushData;
import com.kickstarter.services.apiresponses.NotificationEnvelope;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DebugNotificationsView extends ScrollView {
  @Inject Notifications notifications;

  public DebugNotificationsView(@NonNull final Context context) {
    this(context, null);
  }

  public DebugNotificationsView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DebugNotificationsView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    if (isInEditMode()) {
      return;
    }

    ((KSApplication) getContext().getApplicationContext()).component().inject(this);
  }

  @OnClick(R.id.register_device_button)
  public void registerDeviceButtonClick() {
    notifications.registerDevice();
  }

  @OnClick(R.id.unregister_device_button)
  public void unregisterDeviceButtonClick() {
    notifications.unregisterDevice();
  }

  @OnClick(R.id.simulate_project_success_button)
  public void simulateProjectLaunchButtonClick() {
    final GCMPushData gcm = GCMPushData.builder()
      .alert("Double Fine Adventure has been successfully funded!")
      .build();

    final ActivityPushData activity = ActivityPushData.builder()
      .category(Activity.CATEGORY_SUCCESS)
      .id(1)
      .projectId(1929840910L)
      .build();

    final NotificationEnvelope envelope = NotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    notifications.show(envelope);
  }
}
