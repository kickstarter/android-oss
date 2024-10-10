package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNotificationsBinding
import com.kickstarter.libs.utils.AnimationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.User
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.NotificationsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class NotificationsActivity : ComponentActivity() {

    private val green = R.color.kds_create_700
    private val grey = R.color.kds_support_400

    private val circleOutline = R.drawable.circle_gray_outline
    private val circleFilled = R.drawable.circle_gray_filled

    private val subscribeString = R.string.profile_settings_accessibility_subscribe_notifications
    private val subscribeMobileString =
        R.string.profile_settings_accessibility_subscribe_mobile_notifications
    private val unableToSaveString = R.string.profile_settings_error
    private val unsubscribeMobileString =
        R.string.profile_settings_accessibility_unsubscribe_mobile_notifications
    private val unsubscribeString =
        R.string.profile_settings_accessibility_unsubscribe_notifications

    private var notifyMobileOfBackings: Boolean = false
    private var notifyMobileOfComments: Boolean = false
    private var notifyMobileOfCommentReplies: Boolean = false
    private var notifyMobileOfCreatorEdu: Boolean = false
    private var notifyMobileOfFollower: Boolean = false
    private var notifyMobileOfFriendActivity: Boolean = false
    private var notifyMobileOfMessages: Boolean = false
    private var notifyMobileOfPostLikes: Boolean = false
    private var notifyMobileOfUpdates: Boolean = false
    private var notifyMobileOfMarketingUpdates: Boolean = false
    private var notifyOfBackings: Boolean = false
    private var notifyOfComments: Boolean = false
    private var notifyOfCreatorDigest: Boolean = false
    private var notifyOfCreatorEdu: Boolean = false
    private var notifyOfFollower: Boolean = false
    private var notifyOfFriendActivity: Boolean = false
    private var notifyOfMessages: Boolean = false
    private var notifyOfUpdates: Boolean = false

    private lateinit var binding: ActivityNotificationsBinding

    private lateinit var viewModelFactory: NotificationsViewModel.Factory
    private val viewModel: NotificationsViewModel.NotificationsViewModel by viewModels { viewModelFactory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        this.getEnvironment()?.let { env ->
            viewModelFactory = NotificationsViewModel.Factory(env)
            env
        }

        setContentView(binding.root)

        this.viewModel.outputs.creatorDigestFrequencyIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.emailFrequencyRow.isGone = it
            }.addToDisposable(disposables)

        this.viewModel.outputs.creatorNotificationsAreGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.creatorNotificationsSection.isGone = it
            }.addToDisposable(disposables)

        this.viewModel.outputs.user()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it) }
            .addToDisposable(disposables)

        this.viewModel.errors.unableToSavePreferenceError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _ -> ViewUtils.showToast(this, getString(this.unableToSaveString)) }
            .addToDisposable(disposables)

        val emailFrequencyStrings = User.EmailFrequency.getStrings(this.resources)
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item_spinner, emailFrequencyStrings)
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.emailFrequencySpinner.adapter = arrayAdapter

        setUpClickListeners()
    }

    private fun displayPreferences(user: User) {
        binding.projectNotificationsCount.text =
            user.backedProjectsCount().intValueOrZero().toString()

        displayMarketingUpdates(user)
        displayBackingsNotificationSettings(user)
        displayCommentsNotificationSettings(user)
        displayCommentRepliesNotificationSettings(user)
        displayCreatorTipsNotificationSettings(user)
        displayFollowerNotificationSettings(user)
        displayFriendActivityNotificationSettings(user)
        displayMessagesNotificationSettings(user)
        displayPostLikesNotificationSettings(user)
        displayUpdatesNotificationSettings(user)
    }

    private fun displayMarketingUpdates(user: User) {
        this.notifyMobileOfMarketingUpdates = user.notifyMobileOfMarketingUpdate().isTrue()
        toggleImageButtonIconColor(
            binding.marketingUpdatesPhoneIcon,
            this.notifyMobileOfMarketingUpdates,
            true
        )
    }

    private fun displayBackingsNotificationSettings(user: User) {
        this.notifyMobileOfBackings = user.notifyMobileOfBackings().isTrue()
        this.notifyOfBackings = user.notifyOfBackings().isTrue()
        this.notifyOfCreatorDigest = user.notifyOfCreatorDigest().isTrue()

        val frequencyIndex = when {
            notifyOfCreatorDigest -> User.EmailFrequency.DAILY_SUMMARY.ordinal
            else -> User.EmailFrequency.TWICE_A_DAY_SUMMARY.ordinal
        }

        toggleImageButtonIconColor(binding.backingsPhoneIcon, this.notifyMobileOfBackings, true)
        toggleImageButtonIconColor(binding.backingsMailIcon, this.notifyOfBackings)

        if (frequencyIndex != binding.emailFrequencySpinner.selectedItemPosition) {
            binding.emailFrequencySpinner.setSelection(frequencyIndex, false)
        }

        binding.emailFrequencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (frequencyIndex != position) {
                        viewModel.inputs.notifyOfCreatorDigest(position == User.EmailFrequency.DAILY_SUMMARY.ordinal)
                    }
                }
            }
    }

    private fun displayCommentsNotificationSettings(user: User) {
        this.notifyMobileOfComments = user.notifyMobileOfComments().isTrue()
        this.notifyOfComments = user.notifyOfComments().isTrue()

        toggleImageButtonIconColor(binding.commentsPhoneIcon, this.notifyMobileOfComments, true)
        toggleImageButtonIconColor(binding.commentsMailIcon, this.notifyOfComments)
    }

    private fun displayCommentRepliesNotificationSettings(user: User) {
        this.notifyMobileOfCommentReplies = user.notifyOfCommentReplies().isTrue()
        toggleImageButtonIconColor(
            binding.commentRepliesMailIcon,
            this.notifyMobileOfCommentReplies
        )
    }

    private fun displayCreatorTipsNotificationSettings(user: User) {
        this.notifyMobileOfCreatorEdu = user.notifyMobileOfCreatorEdu().isTrue()
        this.notifyOfCreatorEdu = user.notifyOfCreatorEdu().isTrue()

        toggleImageButtonIconColor(binding.creatorEduPhoneIcon, this.notifyMobileOfCreatorEdu, true)
        toggleImageButtonIconColor(binding.creatorEduMailIcon, this.notifyOfCreatorEdu)
    }

    private fun displayFollowerNotificationSettings(user: User) {
        this.notifyMobileOfFollower = user.notifyMobileOfFollower().isTrue()
        this.notifyOfFollower = user.notifyOfFollower().isTrue()

        toggleImageButtonIconColor(binding.newFollowersPhoneIcon, this.notifyMobileOfFollower, true)
        toggleImageButtonIconColor(binding.newFollowersMailIcon, this.notifyOfFollower)
    }

    private fun displayFriendActivityNotificationSettings(user: User) {
        this.notifyMobileOfFriendActivity = user.notifyMobileOfFriendActivity().isTrue()
        this.notifyOfFriendActivity = user.notifyOfFriendActivity().isTrue()

        toggleImageButtonIconColor(
            binding.friendActivityPhoneIcon,
            this.notifyMobileOfFriendActivity,
            true
        )
        toggleImageButtonIconColor(binding.friendActivityMailIcon, this.notifyOfFriendActivity)
    }

    private fun displayMessagesNotificationSettings(user: User) {
        this.notifyMobileOfMessages = user.notifyMobileOfMessages().isTrue()
        this.notifyOfMessages = user.notifyOfMessages().isTrue()

        toggleImageButtonIconColor(binding.messagesPhoneIcon, this.notifyMobileOfMessages, true)
        toggleImageButtonIconColor(binding.messagesMailIcon, this.notifyOfMessages)
    }

    private fun displayPostLikesNotificationSettings(user: User) {
        this.notifyMobileOfPostLikes = user.notifyMobileOfPostLikes().isTrue()

        toggleImageButtonIconColor(binding.postLikesPhoneIcon, this.notifyMobileOfPostLikes, true)
    }

    private fun displayUpdatesNotificationSettings(user: User) {
        this.notifyMobileOfUpdates = user.notifyMobileOfUpdates().isTrue()
        this.notifyOfUpdates = user.notifyOfUpdates().isTrue()

        toggleImageButtonIconColor(
            binding.projectUpdatesPhoneIcon,
            this.notifyMobileOfUpdates,
            true
        )
        toggleImageButtonIconColor(binding.projectUpdatesMailIcon, this.notifyOfUpdates)
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
        binding.manageProjectNotifications.setOnClickListener {
            startProjectNotificationsSettingsActivity()
        }

        binding.backingsMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfBackings(!this.notifyOfBackings)
        }

        binding.backingsPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfBackings(!this.notifyMobileOfBackings)
        }

        binding.backingsRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.backingsPhoneIcon,
                binding.backingsMailIcon
            )
        }

        binding.commentsMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfComments(!this.notifyOfComments)
        }

        binding.commentsPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfComments(!this.notifyMobileOfComments)
        }

        binding.commentRepliesMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfCommentReplies(!this.notifyMobileOfCommentReplies)
        }

        binding.commentRepliesRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(null, binding.commentRepliesMailIcon)
        }

        binding.commentsRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.commentsPhoneIcon,
                binding.commentsMailIcon
            )
        }

        binding.creatorEduMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfCreatorEdu(!this.notifyOfCreatorEdu)
        }

        binding.creatorEduPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfCreatorEdu(!this.notifyMobileOfCreatorEdu)
        }

        binding.creatorEduRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.creatorEduPhoneIcon,
                binding.creatorEduMailIcon
            )
        }

        binding.friendActivityMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfFriendActivity(!this.notifyOfFriendActivity)
        }

        binding.friendActivityPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfFriendActivity(!this.notifyMobileOfFriendActivity)
        }

        binding.friendsBackProjectRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.friendActivityPhoneIcon,
                binding.friendActivityMailIcon
            )
        }

        binding.messagesMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfMessages(!this.notifyOfMessages)
        }

        binding.messagesPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfMessages(!this.notifyMobileOfMessages)
        }

        binding.messagesNotificationRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.messagesPhoneIcon,
                binding.messagesMailIcon
            )
        }

        binding.newFollowersMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfFollower(!this.notifyOfFollower)
        }

        binding.newFollowersPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfFollower(!this.notifyMobileOfFollower)
        }

        binding.newFollowersRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.newFollowersPhoneIcon,
                binding.newFollowersMailIcon
            )
        }

        binding.postLikesPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfPostLikes(!this.notifyMobileOfPostLikes)
        }

        binding.projectUpdatesMailIcon.setOnClickListener {
            this.viewModel.inputs.notifyOfUpdates(!this.notifyOfUpdates)
        }

        binding.projectUpdatesPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfUpdates(!this.notifyMobileOfUpdates)
        }

        binding.marketingUpdatesPhoneIcon.setOnClickListener {
            this.viewModel.inputs.notifyMobileOfMarketingUpdate(!this.notifyMobileOfMarketingUpdates)
        }

        binding.projectUpdatesRow.setOnClickListener {
            AnimationUtils.notificationBounceAnimation(
                binding.projectUpdatesPhoneIcon,
                binding.projectUpdatesMailIcon
            )
        }
    }

    private fun startProjectNotificationsSettingsActivity() {
        startActivity(Intent(this, ProjectNotificationSettingsActivity::class.java))
    }

    private fun toggleImageButtonIconColor(
        imageButton: ImageButton,
        enabled: Boolean,
        typeMobile: Boolean = false
    ) {
        val color = getEnabledColorResId(enabled)
        imageButton.setColorFilter(ContextCompat.getColor(this, color))

        val background = getEnabledBackgroundResId(enabled)
        imageButton.setBackgroundResource(background)

        setContentDescription(imageButton, enabled, typeMobile)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
