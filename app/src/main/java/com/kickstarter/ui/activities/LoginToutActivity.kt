package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import com.facebook.AccessToken
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.views.LoginPopupMenu
import com.kickstarter.viewmodels.LoginToutViewModel

import java.util.Arrays

import rx.android.schedulers.AndroidSchedulers

import com.kickstarter.libs.utils.TransitionUtils.slideInFromRight
import com.kickstarter.libs.utils.TransitionUtils.transition
import kotlinx.android.synthetic.main.login_toolbar.*
import kotlinx.android.synthetic.main.login_tout_layout.*

@RequiresActivityViewModel(LoginToutViewModel.ViewModel::class)
class LoginToutActivity : BaseActivity<LoginToutViewModel.ViewModel>() {

    private val loginOrSignUpString = R.string.login_tout_navbar_title
    private val loginErrorTitleString = R.string.login_errors_title
    private val unableToLoginString = R.string.login_errors_unable_to_log_in
    private val errorTitleString = R.string.general_error_oops
    private val troubleLoggingInString = R.string.login_tout_errors_facebook_authorization_exception_message
    private val tryAgainString = R.string.login_tout_errors_facebook_authorization_exception_button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_tout_layout)
        login_toolbar.setTitle(getString(this.loginOrSignUpString))

        this.viewModel.outputs.finishWithSuccessfulResult()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { finishWithSuccessfulResult() }

        this.viewModel.outputs.startLoginActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startLogin() }

        this.viewModel.outputs.startSignupActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startSignup() }

        this.viewModel.outputs.startFacebookConfirmationActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startFacebookConfirmationActivity(it.first, it.second) }

        this.viewModel.outputs.showFacebookAuthorizationErrorDialog()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.showDialog(this, getString(this.errorTitleString), getString(this.troubleLoggingInString), getString(this.tryAgainString)) }

        showErrorMessageToasts()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ViewUtils.showToast(this))

        this.viewModel.outputs.startTwoFactorChallenge()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startTwoFactorFacebookChallenge() }

        this.viewModel.outputs.showUnauthorizedErrorDialog()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.showDialog(this, getString(this.loginErrorTitleString), it) }

        disclaimer_text_view.setOnClickListener { LoginPopupMenu(this, help_button).show() }
        facebook_login_button.setOnClickListener {
            this.viewModel.inputs.facebookLoginClick(this,
                    Arrays.asList(*resources.getStringArray(R.array.facebook_permissions_array)))
        }
        login_button.setOnClickListener { this.viewModel.inputs.loginClick() }
        sign_up_button.setOnClickListener { this.viewModel.inputs.signupClick() }
    }

    private fun showErrorMessageToasts() =
            this.viewModel.outputs.showMissingFacebookEmailErrorToast()
                    .map(ObjectUtils.coalesceWith(getString(this.unableToLoginString)))
                    .mergeWith(
                            this.viewModel.outputs.showFacebookInvalidAccessTokenErrorToast()
                                    .map(ObjectUtils.coalesceWith(getString(this.unableToLoginString)))
                    )

    private fun finishWithSuccessfulResult() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun startFacebookConfirmationActivity(facebookUser: ErrorEnvelope.FacebookUser,
                                                  accessTokenString: String) {
        val intent = Intent(this, FacebookConfirmationActivity::class.java)
                .putExtra(IntentKey.FACEBOOK_USER, facebookUser)
                .putExtra(IntentKey.FACEBOOK_TOKEN, accessTokenString)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        transition(this, slideInFromRight())
    }

    private fun startLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        transition(this, slideInFromRight())
    }

    private fun startSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        transition(this, slideInFromRight())
    }

    private fun startTwoFactorFacebookChallenge() {
        val intent = Intent(this, TwoFactorActivity::class.java)
                .putExtra(IntentKey.FACEBOOK_LOGIN, true)
                .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken().token)

        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        transition(this, slideInFromRight())
    }
}
