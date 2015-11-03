package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DebugPushNotificationsView extends ScrollView {
  @Inject PushNotifications pushNotifications;

  private static final String PROJECT_PHOTO_URL = "https://ksr-ugc.imgix.net/projects/73409/photo-original.jpg?v=1397770628&w=160&h=120&fit=crop&auto=format&q=92&s=da84cc0a09478bebca4d8d35f98f77b4";
  private static final Long PROJECT_ID = 1929840910L;

  public DebugPushNotificationsView(@NonNull final Context context) {
    this(context, null);
  }

  public DebugPushNotificationsView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DebugPushNotificationsView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
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
    pushNotifications.registerDevice();
  }

  @OnClick(R.id.unregister_device_button)
  public void unregisterDeviceButtonClick() {
    pushNotifications.unregisterDevice();
  }

  @OnClick(R.id.simulate_friend_follow_button)
  public void simulateFriendFollowButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Christopher Wright is following you on Kickstarter!")
      .title("Kickstarter")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_FOLLOW)
      .id(1)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_cancellation_button)
  public void simulateProjectCancellationButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Double Fine Adventure has been canceled.")
      .title("Kickstarter")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_CANCELLATION)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO_URL)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_failure_button)
  public void simulateProjectFailureButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Double Fine Adventure was not successfully funded.")
      .title("Kickstarter")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_FAILURE)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO_URL)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_launch_button)
  public void simulateProjectLaunchButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Double Fine launched a project!")
      .title("Kickstarter")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_LAUNCH)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO_URL)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_reminder_button)
  public void simulateProjectReminderButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Reminder! Double Fine Adventure is ending soon.")
      .title("Kickstarter")
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .gcm(gcm)
      .project(PushNotificationEnvelope.Project.builder().id(PROJECT_ID).photoUrl(PROJECT_PHOTO_URL).build())
      .build();

    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_success_button)
  public void simulateProjectSuccessButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Double Fine Adventure has been successfully funded!")
      .title("Kickstarter")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_SUCCESS)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO_URL)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_update_button)
  public void simulateProjectUpdateButtonClick() {
    final GCM gcm = GCM.builder()
      .alert("Update #1 posted by Double Fine Adventure.")
      .title("Kickstarter")
      .build();


    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_UPDATE)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO_URL)
      .updateId(175622L)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .activity(activity)
      .gcm(gcm)
      .build();

    pushNotifications.add(envelope);
  }
}
