package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.coalesceWithV2
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.models.chrome.ChromeTabsHelper
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.login.LoginToutScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.viewmodels.LoginToutViewModel
import com.kickstarter.viewmodels.OAuthViewModel
import com.kickstarter.viewmodels.OAuthViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginToutActivity : ComponentActivity() {

    private lateinit var ksString: KSString

    private lateinit var viewModelFactory: LoginToutViewModel.Factory
    private val viewModel: LoginToutViewModel.LoginToutViewmodel by viewModels {
        viewModelFactory
    }

    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    private var environment: Environment? = null

    private val disposables = CompositeDisposable()

    private lateinit var oAuthViewModelFactory: OAuthViewModelFactory
    private val oAuthViewModel: OAuthViewModel by viewModels {
        oAuthViewModelFactory
    }

    private val oAuthLogcat = "OAuth: "

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val url = it.getStringExtra(IntentKey.OAUTH_REDIRECT_URL) ?: ""
                // - Redirection takes place from WebView, as default browser is not Chrome
                afterRedirection(url, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var darkModeEnabled = false

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = LoginToutViewModel.Factory(env)
            oAuthViewModelFactory = OAuthViewModelFactory(environment = env)
            this.ksString = requireNotNull(env.ksString())
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        setContent {
            KickstarterApp(
                useDarkTheme =
                if (darkModeEnabled) {
                    when (theme) {
                        AppThemes.MATCH_SYSTEM.ordinal -> isSystemInDarkTheme()
                        AppThemes.DARK.ordinal -> true
                        AppThemes.LIGHT.ordinal -> false
                        else -> false
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isSystemInDarkTheme() // Force dark mode uses system theme
                } else false
            ) {
                LoginToutScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onFacebookButtonClicked = { facebookLoginClick() },
                    onTermsOfUseClicked = { viewModel.inputs.disclaimerItemClicked(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = {
                        viewModel.inputs.disclaimerItemClicked(
                            DisclaimerItems.PRIVACY
                        )
                    },
                    onCookiePolicyClicked = { viewModel.inputs.disclaimerItemClicked(DisclaimerItems.COOKIES) },
                    onHelpClicked = {
                        viewModel.inputs.disclaimerItemClicked(DisclaimerItems.HELP)
                    },
                    onSignUpOrLogInClicked = {
                        oAuthViewModel.produceState(intent = intent)
                    }
                )
            }
        }

        logInAndSignUpAndLoginWithFacebookVM()

        setUpOAuthViewModel()
    }

    /***
     * Handles the the viewModel RXJava subscriptions for the user cases:
     * - LogIn non OAuth
     * - SignUp non OAuth
     * - LogIn with Facebook
     */
    private fun logInAndSignUpAndLoginWithFacebookVM() {
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
            .subscribe { ViewUtils.showToast(this, it ?: "") }
            .addToDisposable(disposables)

        viewModel.outputs.showUnauthorizedErrorDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.showDialog(
                    this,
                    getString(R.string.login_tout_navbar_title),
                    it
                )
            }
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

        viewModel.outputs.showDisclaimerActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startDisclaimerChromeTab(it, environment)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startResetPasswordActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startResetActivity()
            }
            .addToDisposable(disposables)

        viewModel.outputs.finishOauthWithSuccessfulResult()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishWithSuccessfulResult() }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        Timber.d("$oAuthLogcat onDestroy")
        super.onDestroy()
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("$oAuthLogcat onNewIntent Intent: $intent, data: ${intent?.data}")
        intent?.let {
            val url = intent.data.toString()
            // - Redirection takes place from ChromeTab, as the defaultBrowser is Chrome
            afterRedirection(url, it)
        }
    }

    private fun afterRedirection(url: String, intent: Intent) {
        val uri = Uri.parse(url)
        uri?.let {
            if (OAuthViewModel.isAfterRedirectionStep(it))
                oAuthViewModel.produceState(intent = intent, uri)
        }
    }

    private fun setUpOAuthViewModel() {
        lifecycleScope.launch {
            oAuthViewModel.uiState.collect { state ->
                // - Intent generated with onCreate
                if (state.isAuthorizationStep && state.authorizationUrl.isNotEmpty()) {
                    openChromeTabOrWebViewWithUrl(state.authorizationUrl)
                }

                if (state.user.isNotNull()) {
                    setResult(RESULT_OK)
                    this@LoginToutActivity.finish()
                }
            }
        }
    }

    /**
     * If default Browser is Chrome a CustomChromeTab will be open with give URL
     * If default Browser is not Chrome Webview will be open with given URL
     */
    private fun openChromeTabOrWebViewWithUrl(url: String) {
        val authorizationUri = Uri.parse(url)

        val tabIntent = CustomTabsIntent.Builder().build()

        val packageName = ChromeTabsHelper.getPackageNameToUse(this)
        if (packageName == "com.android.chrome") {
            tabIntent.intent.setPackage(packageName)
            tabIntent.launchUrl(this, authorizationUri)
        } else {
            val intent: Intent = Intent(this, OAuthWebViewActivity::class.java)
                .putExtra(IntentKey.URL, authorizationUri.toString())
            startForResult.launch(intent)
            this.overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }
    }

    private fun facebookLoginClick() =
        viewModel.inputs.facebookLoginClick(
            this,
            resources.getStringArray(R.array.facebook_permissions_array).asList()
        )

    private fun showErrorMessageToasts(): Observable<String?> {
        return viewModel.outputs.showMissingFacebookEmailErrorToast()
            .map(coalesceWithV2(getString(R.string.login_errors_unable_to_log_in)))
            .mergeWith(
                viewModel.outputs.showFacebookInvalidAccessTokenErrorToast()
                    .map(coalesceWithV2(getString(R.string.login_errors_unable_to_log_in)))
            )
    }

    private fun finishWithSuccessfulResult() {
        setResult(RESULT_OK)
        goToSurveyIfSurveyPresent()
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

    @Deprecated("Needs to be replaced with new method, but requires request code usage to go away as well")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.provideOnActivityResult(create(requestCode, resultCode, intent))
    }

    private fun goToSurveyIfSurveyPresent() {
        val surveyResponseDeeplink = IntentCompat.getParcelableExtra(intent, IntentKey.DEEPLINK_SURVEY_RESPONSE, String::class.java)

        surveyResponseDeeplink?.let {
            startSurveyResponseActivity(surveyResponseDeeplink)
        }
    }

    private fun startSurveyResponseActivity(surveyResponseUrl: String) {
        ApplicationUtils.startNewDiscoveryActivity(this)
        val intent = Intent(this, SurveyResponseActivity::class.java)
            .putExtra(IntentKey.DEEPLINK_SURVEY_RESPONSE, surveyResponseUrl)
        startActivity(intent)
        finish()
    }
}

enum class DisclaimerItems(@StringRes val itemName: Int) {
    TERMS(R.string.login_tout_help_sheet_terms),
    COOKIES(R.string.login_tout_help_sheet_cookie),
    PRIVACY(R.string.login_tout_help_sheet_privacy),
    HELP(R.string.general_navigation_buttons_help)
}
