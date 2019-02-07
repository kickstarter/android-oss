package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.AnimationUtils
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.IntegerUtils.intValueOrZero
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.viewmodels.NotificationsViewModel
import kotlinx.android.synthetic.main.activity_notifications.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(NotificationsViewModel.ViewModel::class)
class NotificationsActivity : BaseActivity<NotificationsViewModel.ViewModel>() {

    private val green = R.color.ksr_green_500
    private val grey = R.color.ksr_dark_grey_400

    private val circleOutline = R.drawable.circle_gray_outline
    private val circleFilled = R.drawable.circle_gray_filled

    private val subscribeString = R.string.profile_settings_accessibility_subscribe_notifications
    private val subscribeMobileString = R.string.profile_settings_accessibility_subscribe_mobile_notifications
    private val unableToSaveString = R.string.profile_settings_error
    private val unsubscribeMobileString = R.string.profile_settings_accessibility_unsubscribe_mobile_notifications
    private val unsubscribeString = R.string.profile_settings_accessibility_unsubscribe_notifications

    private var notifyMobileOfBackings: Boolean = false
    private var notifyMobileOfComments: Boolean = false
    private var notifyMobileOfCreatorEdu: Boolean = false
    private var notifyMobileOfFollower: Boolean = false
    private var notifyMobileOfFriendActivity: Boolean = false
    private var notifyMobileOfMessages: Boolean = false
    private var notifyMobileOfPostLikes: Boolean = false
    private var notifyMobileOfUpdates: Boolean = false
    private var notifyOfBackings: Boolean = false
    private var notifyOfComments: Boolean = false
    private var notifyOfCreatorDigest: Boolean = false
    private var notifyOfCreatorEdu: Boolean = false
    private var notifyOfFollower: Boolean = false
    private var notifyOfFriendActivity: Boolean = false
    private var notifyOfMessages: Boolean = false
    private var notifyOfUpdates: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        this.viewModel.outputs.creatorDigestFrequencyIsGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(email_frequency_row, it) }

        this.viewModel.outputs.creatorNotificationsAreGone()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.setGone(creator_notifications_section, it) }

        this.viewModel.outputs.user()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.displayPreferences(it) }

        this.viewModel.errors.unableToSavePreferenceError()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ -> ViewUtils.showToast(this, getString(this.unableToSaveString)) }

        val emailFrequencyStrings = User.EmailFrequency.getStrings(this.resources)
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, emailFrequencyStrings)
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        email_frequency_spinner.adapter = arrayAdapter

        setUpClickListeners()

    }

    override fun exitTransition(): Pair<Int, Int> = TransitionUtils.slideUpFromBottom()

    private fun displayPreferences(user: User) {
        project_notifications_count.text = intValueOrZero(user.backedProjectsCount()).toString()

        displayBackingsNotificationSettings(user)
        displayCommentsNotificationSettings(user)
        displayCreatorTipsNotificationSettings(user)
        displayFollowerNotificationSettings(user)
        displayFriendActivityNotificationSettings(user)
        displayMessagesNotificationSettings(user)
        displayPostLikesNotificationSettings(user)
        displayUpdatesNotificationSettings(user)
    }

    private fun displayBackingsNotificationSettings(user: User) {
        this.notifyMobileOfBackings = isTrue(user.notifyMobileOfBackings())
        this.notifyOfBackings = isTrue(user.notifyOfBackings())
        this.notifyOfCreatorDigest = isTrue(user.notifyOfCreatorDigest())

        val frequencyIndex = when {
            notifyOfCreatorDigest -> User.EmailFrequency.DAILY_SUMMARY.ordinal
            else -> User.EmailFrequency.TWICE_A_DAY_SUMMARY.ordinal
        }

        toggleImageButtonIconColor(backings_phone_icon, this.notifyMobileOfBackings, true)
        toggleImageButtonIconColor(backings_mail_icon, this.notifyOfBackings)

        if (frequencyIndex != email_frequency_spinner.selectedItemPosition) {
            email_frequency_spinner.setSelection(frequencyIndex, false)
        }

        email_frequency_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (frequencyIndex != position) {
                    viewModel.inputs.notifyOfCreatorDigest(position == User.EmailFrequency.DAILY_SUMMARY.ordinal)
                }
            }
        }
    }

    private fun displayCommentsNotificationSettings(user: User) {
        this.notifyMobileOfComments = isTrue(user.notifyMobileOfComments())
        this.notifyOfComments = isTrue(user.notifyOfComments())

        toggleImageButtonIconColor(comments_phone_icon, this.notifyMobileOfComments, true)
        toggleImageButtonIconColor(comments_mail_icon, this.notifyOfComments)
    }

    private fun displayCreatorTipsNotificationSettings(user: User) {
        this.notifyMobileOfCreatorEdu = isTrue(user.notifyMobileOfCreatorEdu())
        this.notifyOfCreatorEdu = isTrue(user.notifyOfCreatorEdu())

        toggleImageButtonIconColor(creator_edu_phone_icon, this.notifyMobileOfCreatorEdu, true)
        toggleImageButtonIconColor(creator_edu_mail_icon, this.notifyOfCreatorEdu)
    }

    private fun displayFollowerNotificationSettings(user: User) {
        this.notifyMobileOfFollower = isTrue(user.notifyMobileOfFollower())
        this.notifyOfFollower = isTrue(user.notifyOfFollower())

        toggleImageButtonIconColor(new_followers_phone_icon, this.notifyMobileOfFollower, true)
        toggleImageButtonIconColor(new_followers_mail_icon, this.notifyOfFollower)
    }

    private fun displayFriendActivityNotificationSettings(user: User) {
        this.notifyMobileOfFriendActivity = isTrue(user.notifyMobileOfFriendActivity())
        this.notifyOfFriendActivity = isTrue(user.notifyOfFriendActivity())

        toggleImageButtonIconColor(friend_activity_phone_icon, this.notifyMobileOfFriendActivity, true)
        toggleImageButtonIconColor(friend_activity_mail_icon, this.notifyOfFriendActivity)
    }

    private fun displayMessagesNotificationSettings(user: User) {
        this.notifyMobileOfMessages = isTrue(user.notifyMobileOfMessages())
        this.notifyOfMessages = isTrue(user.notifyOfMessages())

        toggleImageButtonIconColor(messages_phone_icon, this.notifyMobileOfMessages, true)
        toggleImageButtonIconColor(messages_mail_icon, this.notifyOfMessages)
    }

    private fun displayPostLikesNotificationSettings(user: User) {
        this.notifyMobileOfPostLikes = isTrue(user.notifyMobileOfPostLikes())

        toggleImageButtonIconColor(post_likes_phone_icon, this.notifyMobileOfPostLikes, true)
    }

    private fun displayUpdatesNotificationSettings(user: User) {
        this.notifyMobileOfUpdates = isTrue(user.notifyMobileOfUpdates())
        this.notifyOfUpdates = isTrue(user.notifyOfUpdates())

        toggleImageButtonIconColor(project_updates_phone_icon, this.notifyMobileOfUpdates, true)
        toggleImageButtonIconColor(project_updates_mail_icon, this.notifyOfUpdates)
    }

    private fun getEnabledColorResId(enabled: Boolean): Int {
        return if (enabled) this.green else this.grey
    }

    private fun getEnabledBackgroundResId(enabled: Boolean): Int {
        return if (enabled) this.circleFilled else this.circleOutline
    }

    private fun setContentDescription(view: View, enabled: Boolean, typeMobile: Boolean) {
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

    private fun setUpClickListeners() {
        manage_project_notifications.setOnClickListener {
            startProjectNotificationsSettingsActivity()
        }

        backings_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfBackings(!this.notifyOfBackings)
        }

        backings_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfBackings(!this.notifyMobileOfBackings)
        }

        backings_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(backings_phone_icon, backings_mail_icon)
        }

        comments_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfComments(!this.notifyOfComments)
        }

        comments_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfComments(!this.notifyMobileOfComments)
        }

        comments_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(comments_phone_icon, comments_mail_icon)
        }

        creator_edu_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfCreatorEdu(!this.notifyOfCreatorEdu)
        }

        creator_edu_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfCreatorEdu(!this.notifyMobileOfCreatorEdu)
        }

        creator_edu_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(creator_edu_phone_icon, creator_edu_mail_icon)
        }

        friend_activity_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfFriendActivity(!this.notifyOfFriendActivity)
        }

        friend_activity_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfFriendActivity(!this.notifyMobileOfFriendActivity)
        }

        friends_back_project_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(friend_activity_phone_icon, friend_activity_mail_icon)
        }

        messages_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfMessages(!this.notifyOfMessages)
        }

        messages_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfMessages(!this.notifyMobileOfMessages)
        }

        messages_notification_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(messages_phone_icon, messages_mail_icon)
        }

        new_followers_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfFollower(!this.notifyOfFollower)
        }

        new_followers_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfFollower(!this.notifyMobileOfFollower)
        }

        new_followers_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(new_followers_phone_icon, new_followers_mail_icon)
        }

        post_likes_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfPostLikes(!this.notifyMobileOfPostLikes)
        }

        project_updates_mail_icon.setOnClickListener {
            this.viewModel.inputs.notifyOfUpdates(!this.notifyOfUpdates)
        }

        project_updates_phone_icon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfUpdates(!this.notifyMobileOfUpdates)
        }

        project_updates_row.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(project_updates_phone_icon, project_updates_mail_icon)
        }
    }

    private fun startProjectNotificationsSettingsActivity() {
        val intent = Intent(this, ProjectNotificationSettingsActivity::class.java)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun toggleImageButtonIconColor(imageButton: ImageButton, enabled: Boolean, typeMobile: Boolean = false) {
        val color = getEnabledColorResId(enabled)
        imageButton.setColorFilter(ContextCompat.getColor(this, color))

        val background = getEnabledBackgroundResId(enabled)
        imageButton.setBackgroundResource(background)

        setContentDescription(imageButton, enabled, typeMobile)
    }
}
