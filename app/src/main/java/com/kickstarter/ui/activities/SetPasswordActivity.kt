package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySetPasswordBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.viewmodels.SetPasswordViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(SetPasswordViewModel.ViewModel::class)
class SetPasswordActivity : BaseActivity<SetPasswordViewModel.ViewModel>() {
    private lateinit var binding: ActivitySetPasswordBinding
    private var errorTitleString = R.string.general_error_oops

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetPasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.resetPasswordToolbar.loginToolbar)
        binding.resetPasswordToolbar.loginToolbar.setTitle(getString(R.string.FPO_Set_your_password))
        binding.resetPasswordToolbar.backButton.isGone = true
        binding.newPassword.onChange { this.viewModel.inputs.newPassword(it) }
        binding.confirmPassword.onChange { this.viewModel.inputs.confirmPassword(it) }

        binding.savePasswordButton.setOnClickListener {
            viewModel.inputs.changePasswordClicked()
        }

        this.viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.progressBar.isGone = !it
            }

        this.viewModel.outputs.setUserEmail()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.setPasswordHint.text = getString(R.string.FPO_We_will_be_discontinuing_the_ability_to_log_in_via_Facebook, it)
            }

        this.viewModel.outputs.passwordWarning()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.warning.text = when {
                    it != null -> {
                        getString(it)
                    }
                    else -> null
                }
                binding.warning.isVisible = (it != null)
            }

        this.viewModel.outputs.error()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(this.errorTitleString), it) }

        this.viewModel.outputs.isFormSubmitting()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.setFormDisabled(it) }

        this.viewModel.outputs.saveButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.setFormEnabled(it) }

        this.viewModel.outputs.success()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { finish() }
    }

    private fun setFormEnabled(isEnabled: Boolean) {
        binding.savePasswordButton.isEnabled = isEnabled
    }

    private fun setFormDisabled(isDisabled: Boolean) {
        setFormEnabled(!isDisabled)
    }

    override fun back() {
        // Disable back action Gesture
    }
}
