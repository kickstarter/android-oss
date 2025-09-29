package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getLoginActivityIntent
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
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
    private var currentEmail = ""

    private lateinit var environment: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = ResetPasswordViewModel.Factory(env)
        }

        setContent {
            val error = viewModel.outputs.resetError().subscribeAsState(initial = "").value

            val snackBarState = remember { SnackbarHostState() }

            val showProgressbar =
                viewModel.outputs.isFormSubmitting().subscribeAsState(initial = false).value

            val initialValue = viewModel.outputs.prefillEmail().subscribeAsState(initial = "").value

            val titleAndHint =
                viewModel.outputs.resetPasswordScreenStatus().subscribeAsState(initial = null).value

            val darModeEnabled = this.isDarkModeEnabled(env = environment)
            KickstarterApp(useDarkTheme = darModeEnabled) {
                ResetPasswordScreen(
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
                    showProgressBar = showProgressbar,
                    snackBarState = snackBarState
                )
            }

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(snackBarState) {
                        snackBarState.showSnackbar(error)
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
