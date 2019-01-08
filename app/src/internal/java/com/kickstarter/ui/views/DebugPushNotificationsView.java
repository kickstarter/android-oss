package com.kickstarter.ui.views;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class DebugPushNotificationsView extends ScrollView {
  protected @Inject DeviceRegistrarType deviceRegistrar;
  protected @Inject PushNotifications pushNotifications;

  private static final Long MESSAGE_THREAD_ID = 17848074L;
  private static final String PROJECT_PHOTO = "https://ksr-ugc.imgix.net/projects/1176555/photo-original.png?v=1407175667&w=120&h=120&fit=crop&auto=format&q=92&s=2065d33620d4fef280c4c2d451c2fa93";
  private static final String USER_PHOTO = "https://ksr-ugc.imgix.net/avatars/1583412/portrait.original.png?v=1330782076&w=120&h=120&fit=crop&auto=format&q=92&s=a9029da56a3deab8c4b87818433e3430";
  private static final Long PROJECT_ID = 1761344210L;

  public DebugPushNotificationsView(final @NonNull Context context) {
    this(context, null);
  }

  public DebugPushNotificationsView(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DebugPushNotificationsView(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
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
    this.deviceRegistrar.registerDevice();
  }

  @OnClick(R.id.unregister_device_button)
  public void unregisterDeviceButtonClick() {
    this.deviceRegistrar.unregisterDevice();
  }

  @OnClick(R.id.simulate_friend_backing_button)
  public void simulateFriendBackingButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Check it out")
      .alert("Christopher Wright backed SKULL GRAPHIC TEE.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_BACKING)
      .id(1)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    this.pushNotifications.add(envelope);
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
    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_message_button)
  public void simulateMessageButtonClick() {
    final GCM gcm = GCM.builder()
      .title("New message")
      .alert("Native Squad sent you a message about Help Me Transform This Pile of Wood.")
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .gcm(gcm)
      .message(PushNotificationEnvelope.Message.builder().messageThreadId(MESSAGE_THREAD_ID).projectId(PROJECT_ID).build())
      .build();

    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_cancellation_button)
  public void simulateProjectCancellationButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Kickstarter")
      .alert("SKULL GRAPHIC TEE has been canceled.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_CANCELLATION)
      .id(3)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_failure_button)
  public void simulateProjectFailureButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Kickstarter")
      .alert("SKULL GRAPHIC TEE was not successfully funded.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_FAILURE)
      .id(4)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_launch_button)
  public void simulateProjectLaunchButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Want to be the first backer?")
      .alert("Taylor Moore just launched a project!")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_LAUNCH)
      .id(5)
      .projectId(PROJECT_ID)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_reminder_button)
  public void simulateProjectReminderButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Last call")
      .alert("Reminder! SKULL GRAPHIC TEE is ending soon.")
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .gcm(gcm)
      .project(PushNotificationEnvelope.Project.builder().id(PROJECT_ID).photo(PROJECT_PHOTO).build())
      .build();
    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_success_button)
  public void simulateProjectSuccessButtonClick() {
    this.pushNotifications.add(projectSuccessEnvelope());
  }

  @OnClick(R.id.simulate_project_survey_button)
  public void simulateProjectSurveyButtonClick() {
    final GCM gcm = GCM.builder()
      .title("Backer survey")
      .alert("Response needed! Get your reward for backing bugs in the office.")
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder()
      .gcm(gcm)
      .survey(
        PushNotificationEnvelope.Survey.builder()
          .id(18249859L)
          .projectId(PROJECT_ID)
          .build()
      )
      .build();

    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_project_update_button)
  public void simulateProjectUpdateButtonClick() {
    final GCM gcm = GCM.builder()
      .title("News from Taylor Moore")
      .alert("Update #1 posted by SKULL GRAPHIC TEE.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_UPDATE)
      .id(7)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .updateId(1033848L)
      .build();

    final PushNotificationEnvelope envelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
    this.pushNotifications.add(envelope);
  }

  @OnClick(R.id.simulate_burst_button)
  public void simulateBurstClick() {
    final PushNotificationEnvelope baseEnvelope = projectSuccessEnvelope();
    for (int i = 0; i < 100; i++) {
      // Create a different signature for each push notification
      final GCM gcm = baseEnvelope.gcm().toBuilder().alert(Integer.toString(i)).build();
      this.pushNotifications.add(baseEnvelope.toBuilder().gcm(gcm).build());
    }
  }

  private @NonNull PushNotificationEnvelope projectSuccessEnvelope() {
    final GCM gcm = GCM.builder()
      .title("Time to celebrate!")
      .alert("SKULL GRAPHIC TEE has been successfully funded.")
      .build();

    final Activity activity = Activity.builder()
      .category(com.kickstarter.models.Activity.CATEGORY_SUCCESS)
      .id(6)
      .projectId(PROJECT_ID)
      .projectPhoto(PROJECT_PHOTO)
      .build();

    return PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build();
  }
}
