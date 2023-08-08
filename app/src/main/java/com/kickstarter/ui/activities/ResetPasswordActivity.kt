package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ResetPasswordLayoutBinding
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getLoginActivityIntent
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.extensions.text
import com.kickstarter.ui.extensions.transition
import com.kickstarter.viewmodels.ResetPasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ResetPasswordActivity : ComponentActivity() {

    private lateinit var viewModelFactory : ResetPasswordViewModel.Factory
    private val viewModel : ResetPasswordViewModel.ResetPasswordViewModel by viewModels { viewModelFactory }
    private var forgotPasswordString = R.string.forgot_password_title
    private var errorMessageString = R.string.forgot_password_error
    private var errorGenericString = R.string.Something_went_wrong_please_try_again
    private var errorTitleString = R.string.general_error_oops
    private val disposables = CompositeDisposable()

    private lateinit var binding: ResetPasswordLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResetPasswordLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.getEnvironment()?.let { env ->
            viewModelFactory = ResetPasswordViewModel.Factory(env)
        }

        viewModel.configureWith(intent)

        this.viewModel.outputs.resetLoginPasswordSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onResetSuccess()
            }.addToDisposable(disposables)

        this.viewModel.outputs.isFormSubmitting()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ this.setFormDisabled(it) }
                .addToDisposable(disposables)

        this.viewModel.outputs.isFormValid()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ this.setFormEnabled(it) }
                .addToDisposable(disposables)

        this.viewModel.outputs.resetError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(this.errorTitleString), it) }
                .addToDisposable(disposables)

        binding.resetPasswordFormView.resetPasswordButton.setOnClickListener { this.viewModel.inputs.resetPasswordClick() }

        binding.resetPasswordFormView.email.onChange { this.viewModel.inputs.email(it) }

        this.viewModel.outputs.prefillEmail()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.resetPasswordFormView.email.setText(it)
            }.addToDisposable(disposables)


        this.viewModel.outputs.resetPasswordScreenStatus()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.resetPasswordToolbar.loginToolbar.setTitle(getString(it.title))
                it.hint?.let { hint ->
                    binding.resetPasswordHint.setText(hint)
                    binding.resetPasswordHint.isVisible = true
                }
            }.addToDisposable(disposables)


        this.viewModel.outputs.resetFacebookLoginPasswordSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigateToLoginActivity()
            }.addToDisposable(disposables)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                this@ResetPasswordActivity.transition(slideInFromLeft())
            }
        })
    }

    private fun onResetSuccess() {
        setFormEnabled(false)
        val intent =
            Intent().getLoginActivityIntent(this, binding.resetPasswordFormView.email.text(), LoginReason.RESET_PASSWORD)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        setFormEnabled(false)
        val intent = Intent().getLoginActivityIntent(this, binding.resetPasswordFormView.email.text(), LoginReason.RESET_FACEBOOK_PASSWORD)
        startActivityWithTransition(intent, R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
        finish()
    }
    private fun setFormEnabled(isEnabled: Boolean) {
        binding.resetPasswordFormView.resetPasswordButton.isEnabled = isEnabled
    }

    private fun setFormDisabled(isDisabled: Boolean) {
        setFormEnabled(!isDisabled)
    }
}
