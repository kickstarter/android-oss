package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.facebook.AccessToken
import com.kickstarter.R
import com.kickstarter.databinding.LoginToutLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.HelpActivity.Terms
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.LoginToutViewModel
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(LoginToutViewModel.ViewModel::class)
class LoginToutActivity : BaseActivity<LoginToutViewModel.ViewModel>() {

    private lateinit var binding: LoginToutLayoutBinding
    private lateinit var ksString: KSString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginToutLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.ksString = requireNotNull(environment().ksString())
        binding.loginToolbar.loginToolbar.title = getString(R.string.login_tout_navbar_title)

        viewModel.outputs.finishWithSuccessfulResult()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishWithSuccessfulResult() }

        viewModel.outputs.startLoginActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startLogin() }

        viewModel.outputs.startSignupActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startSignup() }

        viewModel.outputs.startFacebookConfirmationActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startFacebookConfirmationActivity(it.first, it.second) }

        viewModel.outputs.showFacebookAuthorizationErrorDialog()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.showDialog(
                    this,
                    getString(R.string.general_error_oops),
                    getString(R.string.login_tout_errors_facebook_authorization_exception_message),
                    getString(R.string.login_tout_errors_facebook_authorization_exception_button)
                )
            }

        showErrorMessageToasts()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ViewUtils.showToast(this))

        viewModel.outputs.startTwoFactorChallenge()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startTwoFactorFacebookChallenge() }

        viewModel.outputs.showUnauthorizedErrorDialog()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(R.string.login_tout_navbar_title), it) }

        viewModel.outputs.showFacebookErrorDialog()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.showAlertDialog(
                    message = getString(R.string.FPO_reset_your_password_dialog_msg),
                    positiveActionTitle = getString(R.string.FPO_Set_new_password),
                    negativeActionTitle = getString(R.string.FPO_login_with_kickstarter),
                    isCancelable = false,
                    positiveAction = {
                        viewModel.inputs.onResetPasswordFacebookErrorDialogClicked()
                    },
                    negativeAction = {
                        viewModel.inputs.onLoginFacebookErrorDialogClicked()
                    }
                )
            }

        binding.facebookLoginButton.setOnClickListener {
            facebookLoginClick()
        }

        binding.loginButton.setOnClickListener {
            loginButtonClick()
        }

        binding.signUpButton.setOnClickListener {
            signupButtonClick()
        }

        viewModel.outputs.showDisclaimerActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(it)
            }

        viewModel.outputs.startResetPasswordActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startResetActivity()
            }

        // create clickable disclaimer spannable
        binding.disclaimerTextView.parseHtmlTag()

        binding.disclaimerTextView.makeLinks(
            Pair(
                getString(DisclaimerItems.TERMS.itemName),
                View.OnClickListener {
                    viewModel.inputs.disclaimerItemClicked(DisclaimerItems.TERMS)
                }
            ),
            Pair(
                getString(DisclaimerItems.PRIVACY.itemName),
                View.OnClickListener {
                    viewModel.inputs.disclaimerItemClicked(DisclaimerItems.PRIVACY)
                }
            ),
            Pair(
                getString(DisclaimerItems.COOKIES.itemName),
                View.OnClickListener {
                    viewModel.inputs.disclaimerItemClicked(DisclaimerItems.COOKIES)
                }
            ),
        )
    }

    private fun startActivity(disclaimerItem: DisclaimerItems) {
        val intent = when (disclaimerItem) {
            DisclaimerItems.TERMS -> Intent(this, Terms::class.java)
            DisclaimerItems.PRIVACY -> Intent(this, HelpActivity.Privacy::class.java)
            DisclaimerItems.COOKIES -> Intent(this, HelpActivity.CookiePolicy::class.java)
        }
        startActivity(intent)
    }

    private fun facebookLoginClick() =
        viewModel.inputs.facebookLoginClick(
            this,
            resources.getStringArray(R.array.facebook_permissions_array).asList()
        )

    private fun loginButtonClick() =
        viewModel.inputs.loginClick()

    private fun signupButtonClick() =
        viewModel.inputs.signupClick()

    private fun showErrorMessageToasts(): Observable<String?> {
        return viewModel.outputs.showMissingFacebookEmailErrorToast()
            .map(ObjectUtils.coalesceWith(getString(R.string.login_errors_unable_to_log_in)))
            .mergeWith(
                viewModel.outputs.showFacebookInvalidAccessTokenErrorToast()
                    .map(ObjectUtils.coalesceWith(getString(R.string.login_errors_unable_to_log_in)))
            )
    }

    private fun finishWithSuccessfulResult() {
        setResult(RESULT_OK)
        finish()
    }

    private fun startResetActivity() {
        val intent = Intent().getResetPasswordIntent(this, isResetPasswordFacebook = true)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startFacebookConfirmationActivity(
        facebookUser: FacebookUser,
        accessTokenString: String
    ) {
        val intent = Intent(this, FacebookConfirmationActivity::class.java)
            .putExtra(IntentKey.FACEBOOK_USER, facebookUser)
            .putExtra(IntentKey.FACEBOOK_TOKEN, accessTokenString)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.fadeIn())
    }

    private fun startLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.fadeIn())
    }

    private fun startSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.fadeIn())
    }

    fun startTwoFactorFacebookChallenge() {
        val intent = Intent(this, TwoFactorActivity::class.java)
            .putExtra(IntentKey.FACEBOOK_LOGIN, true)
            .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken()?.token)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.fadeIn())
    }
}

enum class DisclaimerItems(@StringRes val itemName: Int) {
    TERMS(R.string.login_tout_help_sheet_terms),
    COOKIES(R.string.login_tout_help_sheet_cookie),
    PRIVACY(R.string.login_tout_help_sheet_privacy)
}
