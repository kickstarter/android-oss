package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageButton
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.IntegerUtils.intValueOrZero
import com.kickstarter.models.User
import com.kickstarter.viewmodels.NotificationsViewModel
import kotlinx.android.synthetic.main.activity_notifications.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(NotificationsViewModel.ViewModel::class)
class NotificationsActivity : BaseActivity<NotificationsViewModel.ViewModel>() {

    private val green =  R.color.ksr_green_500
    private val grey = R.color.ksr_dark_grey_400

    private val circleOutline =  R.drawable.circle_gray_outline
    private val circleFilled = R.drawable.circle_gray_filled

    private val subscribeString = R.string.profile_settings_accessibility_subscribe_notifications
    private val subscribeMobileString = R.string.profile_settings_accessibility_subscribe_mobile_notifications
    private val unsubscribeMobileString = R.string.profile_settings_accessibility_unsubscribe_mobile_notifications
    private val unsubscribeString = R.string.profile_settings_accessibility_unsubscribe_notifications

    private var notifyMobileOfFollower: Boolean = false
    private var notifyMobileOfFriendActivity: Boolean = false
    private var notifyMobileOfMessages: Boolean = false
    private var notifyMobileOfUpdates: Boolean = false
    private var notifyOfFollower: Boolean = false
    private var notifyOfFriendActivity: Boolean = false
    private var notifyOfMessages: Boolean = false
    private var notifyOfUpdates: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        this.viewModel.outputs.user()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.displayPreferences(it) })

        manage_project_notifications.setOnClickListener {
            startProjectNotificationsSettingsActivity()
        }

        friend_activity_mail_icon.setOnClickListener{
            this.viewModel.inputs.notifyOfFriendActivity(!this.notifyOfFriendActivity)
        }

        friend_activity_phone_icon.setOnClickListener{
            this.viewModel.inputs.notifyMobileOfFriendActivity(!this.notifyMobileOfFriendActivity)
        }

        messages_mail_icon.setOnClickListener{
            this.viewModel.inputs.notifyOfMessages(!this.notifyOfMessages)
        }

        messages_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfMessages(!this.notifyMobileOfMessages)
        }

        new_followers_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfFollower(!this.notifyOfFollower)
        }

        new_followers_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfFollower(!this.notifyMobileOfFollower)
        }

        project_updates_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfUpdates(!this.notifyOfUpdates)
        }

        project_updates_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfUpdates(!this.notifyMobileOfUpdates)
        }
    }

    private fun displayPreferences(user: User) {
        project_notifications_count.text = intValueOrZero(user.backedProjectsCount()).toString()

        this.notifyMobileOfFriendActivity = isTrue(user.notifyMobileOfFriendActivity())
        this.notifyOfFriendActivity = isTrue(user.notifyOfFriendActivity())
        this.notifyMobileOfMessages = isTrue(user.notifyMobileOfMessages())
        this.notifyOfMessages = isTrue(user.notifyOfMessages())
        this.notifyMobileOfFollower = isTrue(user.notifyMobileOfFollower())
        this.notifyOfFollower = isTrue(user.notifyOfFollower())
        this.notifyMobileOfUpdates = isTrue(user.notifyMobileOfUpdates())
        this.notifyOfUpdates = isTrue(user.notifyOfUpdates())

        toggleImageButtonIconColor(friend_activity_mail_icon, false, this.notifyOfFriendActivity)
        toggleImageButtonIconColor(friend_activity_phone_icon, true, this.notifyMobileOfFriendActivity)
        toggleImageButtonIconColor(messages_mail_icon, false, this.notifyOfMessages)
        toggleImageButtonIconColor(messages_phone_icon, true, this.notifyMobileOfMessages)
        toggleImageButtonIconColor(new_followers_mail_icon, false, this.notifyOfFollower)
        toggleImageButtonIconColor(new_followers_phone_icon, true, this.notifyMobileOfFollower)
        toggleImageButtonIconColor(project_updates_mail_icon, false, this.notifyOfUpdates)
        toggleImageButtonIconColor(project_updates_phone_icon, true, this.notifyMobileOfUpdates)
    }

    private fun toggleImageButtonIconColor(imageButton: ImageButton, typeMobile: Boolean, enabled: Boolean) {
        val color = getEnabledColorResId(enabled)
        imageButton.setColorFilter(ContextCompat.getColor(this, color))

        val background = getEnabledBackgroundResId(enabled)
        imageButton.setBackgroundResource(background)

        setContentDescription(imageButton, typeMobile, enabled)
    }

    private fun getEnabledColorResId(enabled: Boolean): Int {
        return if (enabled) this.green else this.grey
    }

    private fun getEnabledBackgroundResId(enabled: Boolean): Int {
        return if (enabled) this.circleFilled else this.circleOutline
    }

    private fun setContentDescription(view: View, typeMobile: Boolean, enabled: Boolean) {
        var contentDescription = ""
        if (typeMobile && enabled) {
            contentDescription = getString(this.unsubscribeMobileString)
        }
        if (typeMobile && !enabled) {
            contentDescription = getString(this.subscribeMobileString)
        }
        if (!typeMobile && enabled) {
            contentDescription = getString(this.unsubscribeString)
        }
        if (!typeMobile && !enabled) {
            contentDescription = getString(this.subscribeString)
        }
        view.contentDescription = contentDescription
    }

    private fun startProjectNotificationsSettingsActivity() {
        val intent = Intent(this, ProjectNotificationSettingsActivity::class.java)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
