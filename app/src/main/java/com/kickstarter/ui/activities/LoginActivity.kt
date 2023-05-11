package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.databinding.LoginLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.ui.extensions.text
import com.kickstarter.ui.views.ConfirmDialog
import com.kickstarter.viewmodels.LoginViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LoginActivity : AppCompatActivity() {

    private var confirmResetPasswordSuccessDialog: ConfirmDialog? = null
    private lateinit var ksString: KSString

    private val forgotPasswordString = R.string.login_buttons_forgot_password_html
    private val forgotPasswordSentEmailString = R.string.forgot_password_we_sent_an_email_to_email_address_with_instructions_to_reset_your_password
    private val resetPasswordSentEmailString = R.string.forgot_password_we_sent_an_email_to_email_address_with_instructions_to_set_your_password
    private val loginDoesNotMatchString = R.string.login_errors_does_not_match
    private val unableToLoginString = R.string.login_errors_unable_to_log_in
    private val loginString = R.string.login_buttons_log_in
    private val errorTitleString = R.string.login_errors_title
    private lateinit var binding: LoginLayoutBinding

    private lateinit var viewModelFactory: LoginViewModel.Factory
    private val viewModel: LoginViewModel.LoginViewModel by viewModels { viewModelFactory }

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()

        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = LoginViewModel.Factory(env, intent = intent)
            env
        }

        binding = LoginLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.ksString = requireNotNull(env?.ksString())
        binding.loginToolbar.loginToolbar.setTitle(getString(this.loginString))
        binding.loginFormView.forgotYourPasswordTextView.text = ViewUtils.html(getString(this.forgotPasswordString))

        binding.loginFormView.email.onChange { this.viewModel.inputs.email(it) }
        binding.loginFormView.password.onChange { this.viewModel.inputs.password(it) }

        errorMessages()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { e -> ViewUtils.showDialog(this, getString(this.errorTitleString), e) }
            .addToDisposable(disposables)

        this.viewModel.outputs.tfaChallenge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startTwoFactorActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.loginSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)

        this.viewModel.outputs.prefillEmail()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.loginFormView.email.setText(it)
                binding.loginFormView.email.setSelection(it.length)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.showChangedPasswordSnackbar()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.loginToolbar.loginToolbar, R.string.Got_it_your_changes_have_been_saved) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showCreatedPasswordSnackbar()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.loginToolbar.loginToolbar, R.string.Got_it_your_changes_have_been_saved) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showResetPasswordSuccessDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showAndEmail ->
                val show = showAndEmail.first
                val email = showAndEmail.second
                if (show) {
                    resetPasswordSuccessDialog(email.first, showAndEmail.second.second).show()
                } else {
                    resetPasswordSuccessDialog(email.first, showAndEmail.second.second).dismiss()
                }
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.loginButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ this.setLoginButtonEnabled(it) })
            .addToDisposable(disposables)

        binding.loginFormView.forgotYourPasswordTextView.setOnClickListener {
            startResetPasswordActivity()
        }

        binding.loginFormView.loginButton.setOnClickListener {
            this.viewModel.inputs.loginClick()
            this@LoginActivity.hideKeyboard()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@LoginActivity.finishWithAnimation()
            }
        })
    }

    /**
     * Lazily creates a reset password success confirmation dialog and stores it in an instance variable.
     */
    private fun resetPasswordSuccessDialog(email: String, loginReason: LoginReason): ConfirmDialog {
        if (this.confirmResetPasswordSuccessDialog == null) {
            val message = if (loginReason == LoginReason.RESET_FACEBOOK_PASSWORD) {
                this.ksString.format(getString(this.resetPasswordSentEmailString), "email", email)
            } else {
                this.ksString.format(getString(this.resetPasswordSentEmailString), "email", email)
            }

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
            .map(ObjectUtils.coalesceWithV2(getString(this.loginDoesNotMatchString)))
            .mergeWith(
                this.viewModel.outputs.genericLoginError()
                    .map(ObjectUtils.coalesceWithV2(getString(this.unableToLoginString)))
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

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
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
}
