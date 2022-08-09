package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ResetPasswordLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.text
import com.kickstarter.viewmodels.ResetPasswordViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ResetPasswordViewModel.ViewModel::class)
class ResetPasswordActivity : BaseActivity<ResetPasswordViewModel.ViewModel>() {

    private var forgotPasswordString = R.string.forgot_password_title
    private var errorMessageString = R.string.forgot_password_error
    private var errorGenericString = R.string.Something_went_wrong_please_try_again
    private var errorTitleString = R.string.general_error_oops
    private lateinit var binding: ResetPasswordLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResetPasswordLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.viewModel.outputs.resetLoginPasswordSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onResetSuccess()
            }

        this.viewModel.outputs.isFormSubmitting()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ this.setFormDisabled(it) })

        this.viewModel.outputs.isFormValid()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ this.setFormEnabled(it) })

        this.viewModel.outputs.resetError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(this.errorTitleString), it) }

        binding.resetPasswordFormView.resetPasswordButton.setOnClickListener { this.viewModel.inputs.resetPasswordClick() }

        binding.resetPasswordFormView.email.onChange { this.viewModel.inputs.email(it) }

        this.viewModel.outputs.prefillEmail()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.resetPasswordFormView.email.setText(it)
            }

        this.viewModel.outputs.resetPasswordScreenStatus()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.resetPasswordToolbar.loginToolbar.setTitle(getString(it.title))
                it.hint?.let { hint ->
                    binding.resetPasswordHint.setText(hint)
                    binding.resetPasswordHint.isVisible = true
                }
            }

        this.viewModel.outputs.resetFacebookLoginPasswordSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setFormEnabled(false)
                finish()
            }
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
    }

    private fun onResetSuccess() {
        setFormEnabled(false)
        val intent = Intent(this, LoginActivity::class.java)
            .putExtra(IntentKey.EMAIL, binding.resetPasswordFormView.email.text())
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.RESET_PASSWORD)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setFormEnabled(isEnabled: Boolean) {
        binding.resetPasswordFormView.resetPasswordButton.isEnabled = isEnabled
    }

    private fun setFormDisabled(isDisabled: Boolean) {
        setFormEnabled(!isDisabled)
    }
}
