package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import com.kickstarter.R
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
import kotlinx.android.synthetic.main.login_toolbar.*
import kotlinx.android.synthetic.main.reset_password_form_view.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ResetPasswordViewModel.ViewModel::class)
class ResetPasswordActivity : BaseActivity<ResetPasswordViewModel.ViewModel>() {

    private var forgotPasswordString = R.string.forgot_password_title
    private var errorMessageString = R.string.forgot_password_error
    private var errorGenericString = R.string.Something_went_wrong_please_try_again
    private var errorTitleString = R.string.general_error_oops

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.reset_password_layout)
        login_toolbar.setTitle(getString(this.forgotPasswordString))

        this.viewModel.outputs.resetSuccess()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onResetSuccess() }

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

        reset_password_button.setOnClickListener { this.viewModel.inputs.resetPasswordClick() }

        email.onChange { this.viewModel.inputs.email(it) }

        this.viewModel.outputs.prefillEmail()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                email.setText(it)
            }
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
    }

    private fun onResetSuccess() {
        setFormEnabled(false)
        val intent = Intent(this, LoginActivity::class.java)
            .putExtra(IntentKey.EMAIL, email.text())
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.RESET_PASSWORD)
        startActivityWithTransition(intent, R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    private fun setFormEnabled(isEnabled: Boolean) {
        reset_password_button.isEnabled = isEnabled
    }

    private fun setFormDisabled(isDisabled: Boolean) {
        setFormEnabled(!isDisabled)
    }
}
