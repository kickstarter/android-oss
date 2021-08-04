package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityPrivacyBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.BooleanUtils.isFalse
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.viewmodels.PrivacyViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PrivacyViewModel.ViewModel::class)
class PrivacyActivity : BaseActivity<PrivacyViewModel.ViewModel>() {

    private val cancelString = R.string.Cancel
    private val unableToSaveString = R.string.profile_settings_error
    private val yesTurnOffString = R.string.Yes_turn_off

    private var followingConfirmationDialog: AlertDialog? = null

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.viewModel.outputs.hideConfirmFollowingOptOutPrompt()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { SwitchCompatUtils.setCheckedWithoutAnimation(binding.followingSwitch, true) }

        this.viewModel.outputs.showConfirmFollowingOptOutPrompt()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lazyFollowingOptOutConfirmationDialog().show() }

        this.viewModel.errors.unableToSavePreferenceError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, getString(this.unableToSaveString)) }

        this.viewModel.outputs.user()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it) }

        this.viewModel.outputs.hidePrivateProfileRow()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.privateProfileRow.isGone = it
                binding.privateProfileTextView.isGone = it
                binding.publicProfileTextView.isGone = it
            }

        binding.followingSwitch.setOnClickListener { this.viewModel.inputs.optIntoFollowing(binding.followingSwitch.isChecked) }
        binding.privateProfileSwitch.setOnClickListener { this.viewModel.inputs.showPublicProfile(binding.privateProfileSwitch.isChecked) }
        binding.recommendationsSwitch.setOnClickListener { this.viewModel.inputs.optedOutOfRecommendations(binding.recommendationsSwitch.isChecked) }
        binding.settingsRequestData.setOnClickListener { showPrivacyWebpage(Secrets.Privacy.REQUEST_DATA) }
        binding.settingsDeleteAccount.setOnClickListener { showPrivacyWebpage(Secrets.Privacy.DELETE_ACCOUNT) }
    }

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.followingSwitch, isTrue(user.social()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.privateProfileSwitch, isFalse(user.showPublicProfile()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.recommendationsSwitch, isFalse(user.optedOutOfRecommendations()))
    }

    private fun lazyFollowingOptOutConfirmationDialog(): AlertDialog {
        if (this.followingConfirmationDialog == null) {
            this.followingConfirmationDialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.Are_you_sure))
                .setMessage(getString(R.string.If_you_turn_following_off))
                .setNegativeButton(this.cancelString) { _, _ -> this.viewModel.inputs.optOutOfFollowing(false) }
                .setPositiveButton(this.yesTurnOffString) { _, _ -> this.viewModel.inputs.optOutOfFollowing(true) }
                .create()
        }
        return this.followingConfirmationDialog!!
    }

    private fun showPrivacyWebpage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
