package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.DeviceRegistrarType;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DebugPushNotificationsView extends ScrollView {
  protected @Inject DeviceRegistrarType deviceRegistrar;
  protected @Inject PushNotifications pushNotifications;

  private static final String PROJECT_PHOTO = "https://ksr-ugc.imgix.net/projects/73409/photo-original.jpg?v=1397770628&w=120&h=120&fit=crop&auto=format&q=92&s=5adcd60781d0a7644975f845f03a4cf6";
  private static final String USER_PHOTO = "https://ksr-ugc.imgix.net/avatars/1583412/portrait.original.png?v=1330782076&w=120&h=120&fit=crop&auto=format&q=92&s=a9029da56a3deab8c4b87818433e3430";
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
    deviceRegistrar.registerDevice();
  }

  @OnClick(R.id.unregister_device_button)
  public void unregisterDeviceButtonClick() {
    deviceRegistrar.unregisterDevice();
  }

  @OnClick(R.id.simulate_friend_backing_button)
  public void simulateFriendBackingButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Check it out")
      .alert("Christopher Wright backed Double Fine Adventure.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_BACKING)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_friend_follow_button)
  public void simulateFriendFollowButtonClick() {
    final GCM gcm = GCM.builder()
      .title("You're in good company")
      .alert("Christopher Wright is following you on Kickstarter!")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_FOLLOW)
      .id(2)
      .userPhoto(USER_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_cancellation_button)
  public void simulateProjectCancellationButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Kickstarter")
      .alert("Double Fine Adventure has been canceled.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_CANCELLATION)
      .id(3)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_failure_button)
  public void simulateProjectFailureButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Kickstarter")
      .alert("Double Fine Adventure was not successfully funded.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_FAILURE)
      .id(4)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_launch_button)
  public void simulateProjectLaunchButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Want to be the first backer?")
      .alert("Double Fine just launched a project!")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_LAUNCH)
      .id(5)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_reminder_button)
  public void simulateProjectReminderButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Last call")
      .alert("Reminder! Double Fine Adventure is ending soon.")
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .gcm(gcm)
      .project(PushNotificationEnvelope.Project.builder().id(PROJECT_ID).photo(PROJECT_PHOTO).build())
      .build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_success_button)
  public void simulateProjectSuccessButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Time to celebrate!")
      .alert("Double Fine Adventure has been successfully funded.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_SUCCESS)
      .id(6)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_update_button)
  public void simulateProjectUpdateButtonClick() {
    final GCM gcm = GCM.builder()
      .title("News from Double Fine")
      .alert("Update #1 posted by Double Fine Adventure.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_UPDATE)
      .id(7)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .updateId(175622L)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    pushNotifications.add(envelope);
  }
}
