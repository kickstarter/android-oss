package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.kickstarter.R
import com.kickstarter.databinding.TwoFactorLayoutBinding
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.viewmodels.TwoFactorViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class TwoFactorActivity : AppCompatActivity() {
    private lateinit var binding: TwoFactorLayoutBinding
    private lateinit var viewModelFactory: TwoFactorViewModel.Factory
    private val viewModel: TwoFactorViewModel.TwoFactorViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = TwoFactorViewModel.Factory(env)
        }
        binding = TwoFactorLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.loginToolbar.setTitle(getString(R.string.two_factor_title))

        viewModel.outputs.tfaSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)

        viewModel.outputs.formSubmitting()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setFormDisabled(it) }
            .addToDisposable(disposables)

        viewModel.outputs.formIsValid()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setFormEnabled(it) }
            .addToDisposable(disposables)

        errorMessages()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(R.string.login_errors_title), it) }
            .addToDisposable(disposables)

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

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }g

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
}
