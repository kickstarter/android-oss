package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.BooleanUtils.isFalse
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.viewmodels.PrivacyViewModel
import kotlinx.android.synthetic.main.activity_privacy.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PrivacyViewModel.ViewModel::class)
class PrivacyActivity : BaseActivity<PrivacyViewModel.ViewModel>() {

    private val cancelString = R.string.Cancel
    private val unableToSaveString = R.string.profile_settings_error
    private val yesTurnOffString = R.string.Yes_turn_off

    private var followingConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        this.viewModel.outputs.hideConfirmFollowingOptOutPrompt()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ -> SwitchCompatUtils.setCheckedWithoutAnimation(following_switch, true) }

        this.viewModel.outputs.showConfirmFollowingOptOutPrompt()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ -> lazyFollowingOptOutConfirmationDialog().show() }

        this.viewModel.errors.unableToSavePreferenceError()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ -> ViewUtils.showToast(this, getString(this.unableToSaveString)) }

        this.viewModel.outputs.user()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.displayPreferences(it) })

        this.viewModel.outputs.hidePrivateProfileRow()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    ViewUtils.setGone(private_profile_row, it)
                    ViewUtils.setGone(private_profile_text_view, it)
                    ViewUtils.setGone(public_profile_text_view, it)
                })

        following_switch.setOnClickListener { this.viewModel.inputs.optIntoFollowing(following_switch.isChecked) }
        private_profile_switch.setOnClickListener { this.viewModel.inputs.showPublicProfile(private_profile_switch.isChecked) }
        recommendations_switch.setOnClickListener { this.viewModel.inputs.optedOutOfRecommendations(recommendations_switch.isChecked) }
        settings_request_data.setOnClickListener { showPrivacyWebpage(Secrets.Privacy.REQUEST_DATA) }
        settings_delete_account.setOnClickListener { showPrivacyWebpage(Secrets.Privacy.DELETE_ACCOUNT) }
    }

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(following_switch, isTrue(user.social()))
        SwitchCompatUtils.setCheckedWithoutAnimation(private_profile_switch, isFalse(user.showPublicProfile()))
        SwitchCompatUtils.setCheckedWithoutAnimation(recommendations_switch, isFalse(user.optedOutOfRecommendations()))
    }

    private fun lazyFollowingOptOutConfirmationDialog(): AlertDialog {
        if (this.followingConfirmationDialog == null) {
            this.followingConfirmationDialog = AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(getString(R.string.Are_you_sure))
                    .setMessage(getString(R.string.If_you_turn_following_off))
                    .setNegativeButton(this.cancelString) { _, _ -> this.viewModel.inputs.optOutOfFollowing(false) }
                    .setPositiveButton(this.yesTurnOffString, { _, _ -> this.viewModel.inputs.optOutOfFollowing(true) })
                    .create()
        }
        return this.followingConfirmationDialog!!
    }

    private fun showPrivacyWebpage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
