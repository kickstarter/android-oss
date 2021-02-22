package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import butterknife.ButterKnife
import com.kickstarter.R
import com.kickstarter.databinding.TwoFactorLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.TwoFactorViewModel
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(TwoFactorViewModel.ViewModel::class)
class TwoFactorActivity : BaseActivity<TwoFactorViewModel.ViewModel>() {
    private lateinit var binding: TwoFactorLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.two_factor_layout)
        ButterKnife.bind(this)

        binding.loginToolbar.loginToolbar.setTitle(getString(R.string.two_factor_title))

        viewModel.outputs.tfaSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }

        viewModel.outputs.formSubmitting()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setFormDisabled(it) }

        viewModel.outputs.formIsValid()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setFormEnabled(it) }

        errorMessages()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(R.string.login_errors_title), it) }

        binding.twoFactorFormView.code.doOnTextChanged { text, _, _, _ ->
            text?.let { codeEditTextOnTextChanged(it) }
        }

        binding.twoFactorFormView.loginButton.setOnClickListener {
            loginButtonOnClick()
        }

        binding.twoFactorFormView.resendButton.setOnClickListener {
            resendButtonOnClick()
        }
    }

    private fun errorMessages(): Observable<String> {
        return viewModel.outputs.tfaCodeMismatchError().map { getString(R.string.two_factor_error_message) }
            .mergeWith(viewModel.outputs.genericTfaError().map { getString(R.string.login_errors_unable_to_log_in) })
    }

    private fun codeEditTextOnTextChanged(code: CharSequence) {
        viewModel.inputs.code(code.toString())
    }

    private fun resendButtonOnClick() {
        viewModel.inputs.resendClick()
    }

    private fun loginButtonOnClick() {
        viewModel.inputs.loginClick()
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.twoFactorFormView.loginButton.isEnabled = enabled
    }

    private fun setFormDisabled(disabled: Boolean) = setFormEnabled(!disabled)

    override fun exitTransition() = TransitionUtils.slideInFromLeft()
}
