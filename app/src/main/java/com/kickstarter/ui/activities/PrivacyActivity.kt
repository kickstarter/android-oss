package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityPrivacyBinding
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.User
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.PrivacyViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class PrivacyActivity : ComponentActivity() {

    private lateinit var viewModelFactory: PrivacyViewModel.Factory
    private val viewModel: PrivacyViewModel.PrivacyViewModel by viewModels {
        viewModelFactory
    }

    private val cancelString = R.string.Cancel
    private val unableToSaveString = R.string.profile_settings_error
    private val yesTurnOffString = R.string.Yes_turn_off

    private var followingConfirmationDialog: AlertDialog? = null

    private val disposables = CompositeDisposable()

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )

        getEnvironment()?.let { env ->
            viewModelFactory = PrivacyViewModel.Factory(env)
        }

        setContentView(binding.root)

        this.viewModel.outputs.hideConfirmFollowingOptOutPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { SwitchCompatUtils.setCheckedWithoutAnimation(binding.followingSwitch, true) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showConfirmFollowingOptOutPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lazyFollowingOptOutConfirmationDialog().show() }
            .addToDisposable(disposables)

        this.viewModel.errors.unableToSavePreferenceError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, getString(this.unableToSaveString)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.user()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.hidePrivateProfileRow()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.privateProfileRow.isGone = it
                binding.privateProfileTextView.isGone = it
                binding.publicProfileTextView.isGone = it
            }
            .addToDisposable(disposables)

        binding.followingSwitch.setOnClickListener { this.viewModel.inputs.optIntoFollowing(binding.followingSwitch.isChecked) }
        binding.privateProfileSwitch.setOnClickListener { this.viewModel.inputs.showPublicProfile(binding.privateProfileSwitch.isChecked) }
        binding.recommendationsSwitch.setOnClickListener { this.viewModel.inputs.optedOutOfRecommendations(binding.recommendationsSwitch.isChecked) }
        binding.settingsRequestData.setOnClickListener { showPrivacyWebpage(Secrets.Privacy.TRANSCEND_PRIVACY_REQUEST_FLOW) }
        binding.settingsDeleteAccount.setOnClickListener { showPrivacyWebpage(Secrets.Privacy.TRANSCEND_PRIVACY_REQUEST_FLOW) }
    }

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.followingSwitch, user.social().isTrue())
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.privateProfileSwitch, user.showPublicProfile().isFalse())
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.recommendationsSwitch, user.optedOutOfRecommendations().isFalse())
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

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
