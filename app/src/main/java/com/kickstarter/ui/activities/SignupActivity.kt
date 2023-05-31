package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.kickstarter.R
import com.kickstarter.databinding.SignupLayoutBinding
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.LoginPopupMenu
import com.kickstarter.viewmodels.SignupViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: SignupLayoutBinding
    private lateinit var viewModelFactory: SignupViewModel.Factory
    private val viewModel: SignupViewModel.SignupViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = SignupViewModel.Factory(env)
        }
        binding = SignupLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginToolbar.loginToolbar.title = getString(R.string.signup_button)
        viewModel.outputs.signupSuccess()
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

        viewModel.outputs.sendNewslettersIsChecked()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                SwitchCompatUtils.setCheckedWithoutAnimation(binding.signupFormView.newsletterSwitch, it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.errorString()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, null, it) }
            .addToDisposable(disposables)

        binding.signupFormView.newsletterSwitch.setOnClickListener {
            newsLetterSwitchOnToggle()
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

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
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

    private fun newsLetterSwitchOnToggle() {
        viewModel
            .inputs
            .sendNewslettersClick(binding.signupFormView.newsletterSwitch.isChecked)
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
}
