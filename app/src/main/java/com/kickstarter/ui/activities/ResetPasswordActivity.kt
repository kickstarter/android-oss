package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getLoginActivityIntent
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.login.ResetPasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.ui.extensions.transition
import com.kickstarter.viewmodels.ResetPasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ResetPasswordActivity : ComponentActivity() {

    private lateinit var viewModelFactory: ResetPasswordViewModel.Factory
    private val viewModel: ResetPasswordViewModel.ResetPasswordViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()
    private var theme = AppThemes.MATCH_SYSTEM.ordinal
    private var currentEmail = ""

    private var environment: Environment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var darkModeEnabled = false
        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = ResetPasswordViewModel.Factory(env)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        setContent {
            var error = viewModel.outputs.resetError().subscribeAsState(initial = "").value

            var scaffoldState = rememberScaffoldState()

            var showProgressbar =
                viewModel.outputs.isFormSubmitting().subscribeAsState(initial = false).value

            var initialValue = viewModel.outputs.prefillEmail().subscribeAsState(initial = "").value

            var titleAndHint =
                viewModel.outputs.resetPasswordScreenStatus().subscribeAsState(initial = null).value

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
                ResetPasswordScreen(
                    scaffoldState = scaffoldState,
                    title = titleAndHint?.title?.let { titleId ->
                        stringResource(id = titleId)
                    } ?: stringResource(id = R.string.forgot_password_title),
                    hintText = titleAndHint?.hint?.let { hintId ->
                        stringResource(id = hintId)
                    } ?: "",
                    initialEmail = initialValue,
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onTermsOfUseClicked = { startDisclaimerScreen(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = { startDisclaimerScreen(DisclaimerItems.PRIVACY) },
                    onCookiePolicyClicked = { startDisclaimerScreen(DisclaimerItems.COOKIES) },
                    onHelpClicked = { startDisclaimerScreen(DisclaimerItems.HELP) },
                    onResetPasswordButtonClicked = { email ->
                        currentEmail = email
                        viewModel.setEmail(email)
                        viewModel.inputs.resetPasswordClick()
                    },
                    resetButtonEnabled = !showProgressbar,
                    showProgressBar = showProgressbar
                )
            }

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error)
                        viewModel.resetErrorMessage()
                    }
                }
            }
        }

        viewModel.configureWith(intent)

        this.viewModel.outputs.resetLoginPasswordSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onResetSuccess()
            }.addToDisposable(disposables)

        this.viewModel.outputs.resetFacebookLoginPasswordSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigateToLoginActivity()
            }.addToDisposable(disposables)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                this@ResetPasswordActivity.transition(slideInFromLeft())
            }
        })
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun startDisclaimerScreen(disclaimerItems: DisclaimerItems) {
        startDisclaimerChromeTab(disclaimerItems, environment)
    }

    private fun onResetSuccess() {
        val intent =
            Intent().getLoginActivityIntent(this, currentEmail, LoginReason.RESET_PASSWORD)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent =
            Intent().getLoginActivityIntent(this, currentEmail, LoginReason.RESET_FACEBOOK_PASSWORD)
        startActivityWithTransition(intent, R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
        finish()
    }
}
