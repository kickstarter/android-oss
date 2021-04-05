package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.FacebookConfirmationLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.FacebookConfirmationViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(FacebookConfirmationViewModel.ViewModel::class)
class FacebookConfirmationActivity : BaseActivity<FacebookConfirmationViewModel.ViewModel>() {
    private lateinit var binding: FacebookConfirmationLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FacebookConfirmationLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.signUpWithFacebookToolbar.loginToolbar.title = getString(R.string.facebook_confirmation_navbar_title)

        viewModel.outputs.prefillEmail()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { prefillEmail(it) }

        viewModel.outputs.signupSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
        viewModel.outputs.sendNewslettersIsChecked()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { SwitchCompatUtils.setCheckedWithoutAnimation(binding.newsletterSwitch, it) }

        viewModel.outputs.signupError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(R.string.signup_error_title), it) }

        RxView.clicks(binding.newsletterSwitch)
            .compose(bindToLifecycle())
            .subscribe { viewModel.inputs.sendNewslettersClick(binding.newsletterSwitch.isChecked) }

        binding.createNewAccountButton.setOnClickListener {
            createNewAccountClick()
        }

        binding.loginButton.setOnClickListener {
            loginWithEmailClick()
        }
    }

    private fun createNewAccountClick() {
        viewModel.inputs.createNewAccountClick()
    }

    private fun loginWithEmailClick() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return TransitionUtils.slideInFromLeft()
    }

    private fun prefillEmail(email: String) {
        binding.email.text = email
    }
}
