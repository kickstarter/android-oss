package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.kickstarter.R
import com.kickstarter.databinding.LoginToutLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.HelpActivity.Terms
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.LoginToutViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LoginToutActivity : AppCompatActivity() {

    private lateinit var binding: LoginToutLayoutBinding
    private lateinit var ksString: KSString

    private lateinit var viewModelFactory: LoginToutViewModel.Factory
    private val viewModel: LoginToutViewModel.LoginToutViewmodel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginToutLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.getEnvironment()?.let { env ->
            viewModelFactory = LoginToutViewModel.Factory(env)
            this.ksString = requireNotNull(env.ksString())
        }
        binding.loginToolbar.loginToolbar.title = getString(R.string.login_tout_navbar_title)

        val loginReason = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(IntentKey.LOGIN_REASON, LoginReason::class.java)
        } else {
            intent.getSerializableExtra(IntentKey.LOGIN_REASON) as LoginReason?
        }

        loginReason?.let {
            viewModel.provideLoginReason(it)
        }

        viewModel.outputs.finishWithSuccessfulResult()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishWithSuccessfulResult() }
            .addToDisposable(disposables)

        viewModel.outputs.startLoginActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startLogin() }
            .addToDisposable(disposables)

        viewModel.outputs.startSignupActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startSignup() }
            .addToDisposable(disposables)

        viewModel.outputs.startFacebookConfirmationActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startFacebookConfirmationActivity(it.first, it.second) }
            .addToDisposable(disposables)

        viewModel.outputs.showFacebookAuthorizationErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.showDialog(
                    this,
                    getString(R.string.general_error_oops),
                    getString(R.string.login_tout_errors_facebook_authorization_exception_message),
                    getString(R.string.login_tout_errors_facebook_authorization_exception_button)
                )
            }
            .addToDisposable(disposables)

        showErrorMessageToasts()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this) }
            .addToDisposable(disposables)

        viewModel.outputs.startTwoFactorChallenge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startTwoFactorFacebookChallenge() }
            .addToDisposable(disposables)

        viewModel.outputs.showUnauthorizedErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showDialog(this, getString(R.string.login_tout_navbar_title), it) }
            .addToDisposable(disposables)

        viewModel.outputs.showFacebookErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.showAlertDialog(
                    message = getString(R.string.We_can_no_longer_log_you_in_through_Facebook),
                    positiveActionTitle = getString(R.string.Set_new_password),
                    negativeActionTitle = getString(R.string.accessibility_discovery_buttons_log_in),
                    isCancelable = false,
                    positiveAction = {
                        viewModel.inputs.onResetPasswordFacebookErrorDialogClicked()
                    },
                    negativeAction = {
                        viewModel.inputs.onLoginFacebookErrorDialogClicked()
                    }
                )
            }
            .addToDisposable(disposables)

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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startResetPasswordActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startResetActivity()
            }
            .addToDisposable(disposables)

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
            .map(ObjectUtils.coalesceWithV2(getString(R.string.login_errors_unable_to_log_in)))
            .mergeWith(
                viewModel.outputs.showFacebookInvalidAccessTokenErrorToast()
                    .map(ObjectUtils.coalesceWithV2(getString(R.string.login_errors_unable_to_log_in)))
            )
    }

    private fun finishWithSuccessfulResult() {
        setResult(RESULT_OK)
        finish()
    }

    private fun startResetActivity() {
        val intent = Intent().getResetPasswordIntent(this, isResetPasswordFacebook = true)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
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

    private fun startTwoFactorFacebookChallenge() {
        val intent = Intent(this, TwoFactorActivity::class.java)
            .putExtra(IntentKey.FACEBOOK_LOGIN, true)
            .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken()?.token)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.fadeIn())
    }

    @Deprecated("Needs to be replaced with new method, but requires request code usage to go away as well")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.provideOnActivityResult(create(requestCode, resultCode, intent))
    }
}

enum class DisclaimerItems(@StringRes val itemName: Int) {
    TERMS(R.string.login_tout_help_sheet_terms),
    COOKIES(R.string.login_tout_help_sheet_cookie),
    PRIVACY(R.string.login_tout_help_sheet_privacy)
}
