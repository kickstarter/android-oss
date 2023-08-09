package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySetPasswordBinding
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.viewmodels.SetPasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SetPasswordActivity : AppCompatActivity() {
    private lateinit var viewModelFactory: SetPasswordViewModel.Factory
    private val viewModel: SetPasswordViewModel.SetPasswordViewModel by viewModels { viewModelFactory }
    private lateinit var binding: ActivitySetPasswordBinding
    private var errorTitleString = R.string.general_error_oops
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = SetPasswordViewModel.Factory(env)
        }

        binding = ActivitySetPasswordBinding.inflate(layoutInflater)

        viewModel.configureWith(intent)
        setContentView(binding.root)
        setSupportActionBar(binding.resetPasswordToolbar.loginToolbar)
        binding.resetPasswordToolbar.loginToolbar.setTitle(getString(R.string.Set_your_password))
        binding.resetPasswordToolbar.backButton.isGone = true
        binding.newPassword.onChange { this.viewModel.inputs.newPassword(it) }
        binding.confirmPassword.onChange { this.viewModel.inputs.confirmPassword(it) }

        binding.savePasswordButton.setOnClickListener {
            viewModel.inputs.changePasswordClicked()
        }

        this.viewModel.outputs.progressBarIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.progressBar.isGone = !it
            }.addToDisposable(disposables)

        this.viewModel.outputs.setUserEmail()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.setPasswordHint.text = getEnvironment()?.ksString()?.format(getString(R.string.We_will_be_discontinuing_the_ability_to_log_in_via_FB), "email", it)
            }.addToDisposable(disposables)

        this.viewModel.outputs.passwordWarning()
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                binding.warning.text = when {
                    it != null -> {
                        getString(it)
                    }
                    else -> null
                }
                binding.warning.isVisible = (it != null)
            }.addToDisposable(disposables)

        this.viewModel.outputs.error()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(this.errorTitleString), it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.isFormSubmitting()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.setFormDisabled(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.saveButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.setFormEnabled(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finish() }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun setFormEnabled(isEnabled: Boolean) {
        binding.savePasswordButton.isEnabled = isEnabled
    }

    private fun setFormDisabled(isDisabled: Boolean) {
        setFormEnabled(!isDisabled)
    }
}
