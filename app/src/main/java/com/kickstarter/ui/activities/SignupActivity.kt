package com.kickstarter.ui.activities

import android.os.Bundle
import android.util.Pair
import androidx.core.widget.doOnTextChanged
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.SignupLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.LoginPopupMenu
import com.kickstarter.viewmodels.SignupViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(SignupViewModel.ViewModel::class)
class SignupActivity : BaseActivity<SignupViewModel.ViewModel>() {

    private lateinit var binding: SignupLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginToolbar.loginToolbar.title = getString(R.string.signup_button)
        viewModel.outputs.signupSuccess()
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

        viewModel.outputs.sendNewslettersIsChecked()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                SwitchCompatUtils.setCheckedWithoutAnimation(binding.signupFormView.newsletterSwitch, it)
            }

        viewModel.outputs.errorString()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, null, it) }

        RxView.clicks(binding.signupFormView.newsletterSwitch)
            .skip(1)
            .compose(bindToLifecycle())
            .subscribe {
                viewModel
                    .inputs
                    .sendNewslettersClick(binding.signupFormView.newsletterSwitch.isChecked)
            }

        binding.signupFormView.disclaimer.setOnClickListener {
            disclaimerClick()
        }

        binding.signupFormView.signupButton.setOnClickListener {
            signupButtonOnClick()
        }

        binding.signupFormView.name.doOnTextChanged { name, _, _, _ ->
            name?.let { onNameTextChanged(it) }
        }

        binding.signupFormView.email.doOnTextChanged { email, _, _, _ ->
            email?.let { onEmailTextChanged(it) }
        }

        binding.signupFormView.password.doOnTextChanged { password, _, _, _ ->
            password?.let { onPasswordTextChange(it) }
        }
    }

    private fun disclaimerClick() {
        LoginPopupMenu(this, binding.loginToolbar.helpButton).show()
    }

    private fun onNameTextChanged(name: CharSequence) {
        viewModel.inputs.name(name.toString())
    }

    private fun onEmailTextChanged(email: CharSequence) {
        viewModel.inputs.email(email.toString())
    }

    private fun onPasswordTextChange(password: CharSequence) {
        viewModel.inputs.password(password.toString())
    }

    private fun signupButtonOnClick() {
        viewModel.inputs.signupClick()
        this.hideKeyboard()
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.signupFormView.signupButton.isEnabled = enabled
    }

    private fun setFormDisabled(disabled: Boolean) {
        setFormEnabled(!disabled)
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return TransitionUtils.slideInFromLeft()
    }

    override fun back() {
        if (this.supportFragmentManager.backStackEntryCount == 0) {
            super.back()
        }
    }
}
