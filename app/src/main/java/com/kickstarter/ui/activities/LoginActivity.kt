package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.databinding.LoginLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.ui.extensions.text
import com.kickstarter.ui.views.ConfirmDialog
import com.kickstarter.viewmodels.LoginViewModel

@RequiresActivityViewModel(LoginViewModel.ViewModel::class)
class LoginActivity : BaseActivity<LoginViewModel.ViewModel>() {

    private var confirmResetPasswordSuccessDialog: ConfirmDialog? = null
    private lateinit var ksString: KSString

    private val forgotPasswordString = R.string.login_buttons_forgot_password_html
    private val forgotPasswordSentEmailString = R.string.forgot_password_we_sent_an_email_to_email_address_with_instructions_to_reset_your_password
    private val loginDoesNotMatchString = R.string.login_errors_does_not_match
    private val unableToLoginString = R.string.login_errors_unable_to_log_in
    private val loginString = R.string.login_buttons_log_in
    private val errorTitleString = R.string.login_errors_title
    private lateinit var binding: LoginLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.ksString = requireNotNull(environment().ksString())
        binding.loginToolbar.loginToolbar.setTitle(getString(this.loginString))
        binding.loginFormView.forgotYourPasswordTextView.text = ViewUtils.html(getString(this.forgotPasswordString))

        binding.loginFormView.email.onChange { this.viewModel.inputs.email(it) }
        binding.loginFormView.password.onChange { this.viewModel.inputs.password(it) }

        errorMessages()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { e -> ViewUtils.showDialog(this, getString(this.errorTitleString), e) }

        this.viewModel.outputs.tfaChallenge()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { startTwoFactorActivity() }

        this.viewModel.outputs.loginSuccess()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { onSuccess() }

        this.viewModel.outputs.prefillEmail()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.loginFormView.email.setText(it)
                binding.loginFormView.email.setSelection(it.length)
            }

        this.viewModel.outputs.showChangedPasswordSnackbar()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { showSnackbar(binding.loginToolbar.loginToolbar, R.string.Got_it_your_changes_have_been_saved) }

        this.viewModel.outputs.showCreatedPasswordSnackbar()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { showSnackbar(binding.loginToolbar.loginToolbar, R.string.Got_it_your_changes_have_been_saved) }

        this.viewModel.outputs.showResetPasswordSuccessDialog()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { showAndEmail ->
                val show = showAndEmail.first
                val email = showAndEmail.second
                if (show) {
                    resetPasswordSuccessDialog(email).show()
                } else {
                    resetPasswordSuccessDialog(email).dismiss()
                }
            }

        this.viewModel.outputs.loginButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe({ this.setLoginButtonEnabled(it) })

        binding.loginFormView.forgotYourPasswordTextView.setOnClickListener {
            startResetPasswordActivity()
               }

        binding.loginFormView.loginButton.setOnClickListener {
            this.viewModel.inputs.loginClick()
            this@LoginActivity.hideKeyboard()
        }
    }

    /**
     * Lazily creates a reset password success confirmation dialog and stores it in an instance variable.
     */
    private fun resetPasswordSuccessDialog(email: String): ConfirmDialog {
        if (this.confirmResetPasswordSuccessDialog == null) {
            val message = this.ksString.format(getString(this.forgotPasswordSentEmailString), "email", email)
            this.confirmResetPasswordSuccessDialog = ConfirmDialog(this, null, message)

            this.confirmResetPasswordSuccessDialog!!
                .setOnDismissListener { this.viewModel.inputs.resetPasswordConfirmationDialogDismissed() }
            this.confirmResetPasswordSuccessDialog!!
                .setOnCancelListener { this.viewModel.inputs.resetPasswordConfirmationDialogDismissed() }
        }
        return this.confirmResetPasswordSuccessDialog!!
    }

    private fun errorMessages() =
        this.viewModel.outputs.invalidLoginError()
            .map(ObjectUtils.coalesceWith(getString(this.loginDoesNotMatchString)))
            .mergeWith(
                this.viewModel.outputs.genericLoginError()
                    .map(ObjectUtils.coalesceWith(getString(this.unableToLoginString)))
            )

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode != ActivityRequestCodes.LOGIN_FLOW && requestCode != ActivityRequestCodes.RESET_FLOW) {
            return
        }

        if (requestCode != ActivityRequestCodes.RESET_FLOW) {
            setResult(resultCode, intent)
            finish()
        }
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setLoginButtonEnabled(enabled: Boolean) {
        binding.loginFormView.loginButton.isEnabled = enabled
    }

    private fun startTwoFactorActivity() {
        val intent = Intent(this, TwoFactorActivity::class.java)
            .putExtra(IntentKey.EMAIL, binding.loginFormView.email.text())
            .putExtra(IntentKey.PASSWORD, binding.loginFormView.password.text())
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startResetPasswordActivity() {
        val intent = Intent().getResetPasswordIntent(this, email = binding.loginFormView.email.text.toString())
        startActivityForResult(intent, ActivityRequestCodes.RESET_FLOW)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
    }

    override fun back() {
        if (this.supportFragmentManager.backStackEntryCount == 0) {
            super.back()
        }
    }
}
