package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.ScrollView
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.DeviceRegistrarType
import com.kickstarter.libs.PushNotifications
import com.kickstarter.models.Activity
import com.kickstarter.models.pushdata.GCM
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import javax.inject.Inject

class DebugPushNotificationsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {
    @JvmField
    @Inject
    var deviceRegistrar: DeviceRegistrarType? = null

    @JvmField
    @Inject
    var pushNotifications: PushNotifications? = null
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInEditMode) {
            return
        }
        (context.applicationContext as KSApplication).component().inject(this)

        findViewById<Button>(R.id.register_device_button).setOnClickListener {
            registerDeviceButtonClick()
        }

        findViewById<Button>(R.id.unregister_device_button).setOnClickListener {
            unregisterDeviceButtonClick()
        }

        findViewById<Button>(R.id.simulate_errored_pledge_button).setOnClickListener {
            simulateErroredPledgeButtonClick()
        }

        findViewById<Button>(R.id.simulate_friend_backing_button).setOnClickListener {
            simulateFriendBackingButtonClick()
        }

        findViewById<Button>(R.id.simulate_friend_follow_button).setOnClickListener {
            simulateFriendFollowButtonClick()
        }

        findViewById<Button>(R.id.simulate_message_button).setOnClickListener {
            simulateMessageButtonClick()
        }

        findViewById<Button>(R.id.simulate_project_cancellation_button).setOnClickListener {
            simulateProjectCancellationButtonClick()
        }

        findViewById<Button>(R.id.simulate_project_failure_button).setOnClickListener {
            simulateProjectFailureButtonClick()
        }

        findViewById<Button>(R.id.simulate_project_launch_button).setOnClickListener {
            simulateProjectLaunchButtonClick()
        }
        findViewById<Button>(R.id.simulate_project_reminder_button).setOnClickListener {
            simulateProjectReminderButtonClick()
        }

        findViewById<Button>(R.id.simulate_project_success_button).setOnClickListener {
            simulateProjectSuccessButtonClick()
        }

        findViewById<Button>(R.id.simulate_project_survey_button).setOnClickListener {
            simulateProjectSurveyButtonClick()
        }

        findViewById<Button>(R.id.simulate_project_update_button).setOnClickListener {
            simulateProjectUpdateButtonClick()
        }

        findViewById<Button>(R.id.simulate_burst_button).setOnClickListener {
            simulateBurstClick()
        }
    }

    fun registerDeviceButtonClick() {
        deviceRegistrar?.registerDevice()
    }

    fun unregisterDeviceButtonClick() {
        deviceRegistrar?.unregisterDevice()
    }

    fun simulateErroredPledgeButtonClick() {
        val gcm = GCM.builder()
            .title("Payment failure")
            .alert("Response needed! Get your reward for backing SKULL GRAPHIC TEE.")
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .erroredPledge(
                PushNotificationEnvelope.ErroredPledge.builder()
                    .projectId(PROJECT_ID)
                    .build()
            )
            .build()
        pushNotifications?.add(envelope)
    }

    fun simulateFriendBackingButtonClick() {
        val gcm = GCM.builder()
            .title("Check it out")
            .alert("Christopher Wright backed SKULL GRAPHIC TEE.")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_BACKING)
            .id(1)
            .projectId(PROJECT_ID)
            .projectPhoto(PROJECT_PHOTO)
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
        pushNotifications?.add(envelope)
    }

    fun simulateFriendFollowButtonClick() {
        val gcm = GCM.builder()
            .title("You're in good company")
            .alert("Christopher Wright is following you on Kickstarter!")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_FOLLOW)
            .id(2)
            .userPhoto(USER_PHOTO)
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
        pushNotifications?.add(envelope)
    }

    fun simulateMessageButtonClick() {
        val gcm = GCM.builder()
            .title("New message")
            .alert("Native Squad sent you a message about Help Me Transform This Pile of Wood.")
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .message(PushNotificationEnvelope.Message.builder().messageThreadId(MESSAGE_THREAD_ID).projectId(PROJECT_ID).build())
            .build()
        pushNotifications?.add(envelope)
    }

    fun simulateProjectCancellationButtonClick() {
        val gcm = GCM.builder()
            .title("Kickstarter")
            .alert("SKULL GRAPHIC TEE has been canceled.")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_CANCELLATION)
            .id(3)
            .projectId(PROJECT_ID)
            .projectPhoto(PROJECT_PHOTO)
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
        pushNotifications?.add(envelope)
    }

    fun simulateProjectFailureButtonClick() {
        val gcm = GCM.builder()
            .title("Kickstarter")
            .alert("SKULL GRAPHIC TEE was not successfully funded.")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_FAILURE)
            .id(4)
            .projectId(PROJECT_ID)
            .projectPhoto(PROJECT_PHOTO)
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
        pushNotifications?.add(envelope)
    }

    fun simulateProjectLaunchButtonClick() {
        val gcm = GCM.builder()
            .title("Want to be the first backer?")
            .alert("Taylor Moore just launched a project!")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_LAUNCH)
            .id(5)
            .projectId(PROJECT_ID)
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
        pushNotifications?.add(envelope)
    }

    fun simulateProjectReminderButtonClick() {
        val gcm = GCM.builder()
            .title("Last call")
            .alert("Reminder! SKULL GRAPHIC TEE is ending soon.")
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .project(PushNotificationEnvelope.Project.builder().id(PROJECT_ID).photo(PROJECT_PHOTO).build())
            .build()
        pushNotifications?.add(envelope)
    }

    fun simulateProjectSuccessButtonClick() {
        pushNotifications?.add(projectSuccessEnvelope())
    }

    fun simulateProjectSurveyButtonClick() {
        val gcm = GCM.builder()
            .title("Backer survey")
            .alert("Response needed! Get your reward for backing bugs in the office.")
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .survey(
                PushNotificationEnvelope.Survey.builder()
                    .id(18249859L)
                    .projectId(PROJECT_ID)
                    .build()
            )
            .build()
        pushNotifications?.add(envelope)
    }

    fun simulateProjectUpdateButtonClick() {
        val gcm = GCM.builder()
            .title("News from Taylor Moore")
            .alert("Update #1 posted by SKULL GRAPHIC TEE.")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_UPDATE)
            .id(7)
            .projectId(PROJECT_ID)
            .projectPhoto(PROJECT_PHOTO)
            .updateId(1033848L)
            .build()
        val envelope: PushNotificationEnvelope = PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
        pushNotifications?.add(envelope)
    }

    fun simulateBurstClick() {
        val baseEnvelope: PushNotificationEnvelope = projectSuccessEnvelope()
        for (i in 0..99) {
            // Create a different signature for each push notification
            val gcm: GCM = baseEnvelope.gcm().toBuilder().alert(Integer.toString(i)).build()
            pushNotifications?.add(baseEnvelope.toBuilder().gcm(gcm).build())
        }
    }

    private fun projectSuccessEnvelope(): PushNotificationEnvelope {
        val gcm = GCM.builder()
            .title("Time to celebrate!")
            .alert("SKULL GRAPHIC TEE has been successfully funded.")
            .build()
        val activity = com.kickstarter.models.pushdata.Activity.builder()
            .category(Activity.CATEGORY_SUCCESS)
            .id(6)
            .projectId(PROJECT_ID)
            .projectPhoto(PROJECT_PHOTO)
            .build()
        return PushNotificationEnvelope.builder().activity(activity).gcm(gcm).build()
    }

    companion object {
        private const val MESSAGE_THREAD_ID = 17848074L
        private const val PROJECT_PHOTO = "https://ksr-ugc.imgix.net/projects/1176555/photo-original.png?v=1407175667&w=120&h=120&fit=crop&auto=format&q=92&s=2065d33620d4fef280c4c2d451c2fa93"
        private const val USER_PHOTO = "https://ksr-ugc.imgix.net/avatars/1583412/portrait.original.png?v=1330782076&w=120&h=120&fit=crop&auto=format&q=92&s=a9029da56a3deab8c4b87818433e3430"
        private const val PROJECT_ID = 1761344210L
    }
}
