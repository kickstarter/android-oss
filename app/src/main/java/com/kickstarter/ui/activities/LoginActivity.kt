package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.KSString
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.coalesceWithV2
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.login.LoginScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.startDisclaimerActivity
import com.kickstarter.viewmodels.LoginViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LoginActivity : ComponentActivity() {

    private lateinit var ksString: KSString

    private val resetPasswordSentEmailString =
        R.string.forgot_password_we_sent_an_email_to_email_address_with_instructions_to_set_your_password
    private val loginDoesNotMatchString = R.string.login_errors_does_not_match
    private val unableToLoginString = R.string.login_errors_unable_to_log_in

    private lateinit var viewModelFactory: LoginViewModel.Factory
    private val viewModel: LoginViewModel.LoginViewModel by viewModels { viewModelFactory }

    private lateinit var disposables: CompositeDisposable

    private var darkModeEnabled = false
    private var currentEmail = ""
    private var currentPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()

        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = LoginViewModel.Factory(env, intent = intent)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            env
        }

        setContent {
            var prefillEmail = viewModel.outputs.prefillEmail().subscribeAsState(initial = "").value

            var isLoading = viewModel.isLoading().subscribeAsState(initial = false).value

            var error by remember { mutableStateOf("") }

            errorMessages().subscribe {
                error = it
            }.addToDisposable(disposables)

            var scaffoldState = rememberScaffoldState()

            var resetPasswordDialogMessage by rememberSaveable { mutableStateOf("") }

            var showDialog by rememberSaveable { mutableStateOf(false) }

            var showResetPasswordDialog = viewModel.outputs.showResetPasswordSuccessDialog()
                .subscribeAsState(initial = Pair(false, Pair("", LoginReason.DEFAULT))).value
            when {
                showResetPasswordDialog.first -> {
                    val emailAndReason = showResetPasswordDialog.second
                    val message =
                        if (emailAndReason.second == LoginReason.RESET_FACEBOOK_PASSWORD) {
                            this.ksString.format(
                                getString(this.resetPasswordSentEmailString),
                                "email",
                                emailAndReason.first
                            )
                        } else {
                            this.ksString.format(
                                getString(this.resetPasswordSentEmailString),
                                "email",
                                emailAndReason.first
                            )
                        }
                    resetPasswordDialogMessage = message
                    showDialog = true
                }

                else -> {
                    resetPasswordDialogMessage = ""
                    showDialog = false
                }
            }

            var changedPasswordMessage by remember { mutableStateOf("") }

            this.viewModel.outputs.showChangedPasswordSnackbar()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    changedPasswordMessage = getString(R.string.Got_it_your_changes_have_been_saved)
                }
                .addToDisposable(disposables)

            var createPasswordMessage by remember { mutableStateOf("") }

            this.viewModel.outputs.showCreatedPasswordSnackbar()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    createPasswordMessage = getString(R.string.Got_it_your_changes_have_been_saved)
                }
                .addToDisposable(disposables)

            KickstarterApp(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
                LoginScreen(
                    prefillEmail = prefillEmail,
                    scaffoldState = scaffoldState,
                    isLoading = isLoading,
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onLoginClicked = { email, password ->
                        currentEmail = email
                        currentPassword = password
                        viewModel.inputs.email(email)
                        viewModel.inputs.password(password)
                        viewModel.inputs.loginClick()
                    },
                    onTermsOfUseClicked = { startDisclaimerActivity(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = { startDisclaimerActivity(DisclaimerItems.PRIVACY) },
                    onCookiePolicyClicked = { startDisclaimerActivity(DisclaimerItems.COOKIES) },
                    onHelpClicked = { startDisclaimerActivity(DisclaimerItems.HELP) },
                    onForgotPasswordClicked = { startResetPasswordActivity() },
                    resetPasswordDialogMessage = resetPasswordDialogMessage,
                    showDialog = showDialog,
                    setShowDialog = {
                        showDialog = it
                        this.viewModel.inputs.resetPasswordConfirmationDialogDismissed()
                    }
                )
            }

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error, actionLabel = "error")
                        error = ""
                    }
                }

                changedPasswordMessage.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(
                            changedPasswordMessage,
                            actionLabel = "success"
                        )
                        changedPasswordMessage = ""
                    }
                }

                createPasswordMessage.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(
                            createPasswordMessage,
                            actionLabel = "success"
                        )
                        createPasswordMessage = ""
                    }
                }
            }
        }

        this.ksString = requireNotNull(env?.ksString())

        this.viewModel.outputs.tfaChallenge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startTwoFactorActivity() }
            .addToDisposable(disposables)

        this.viewModel.outputs.loginSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@LoginActivity.finishWithAnimation()
            }
        })
    }

    private fun errorMessages() =
        this.viewModel.outputs.invalidLoginError()
            .map(coalesceWithV2(getString(this.loginDoesNotMatchString)))
            .mergeWith(
                this.viewModel.outputs.genericLoginError()
                    .map(coalesceWithV2(getString(this.unableToLoginString)))
            )

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.activityResult(create(requestCode, resultCode, intent))

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

    private fun startTwoFactorActivity() {
        val intent = Intent(this, TwoFactorActivity::class.java)
            .putExtra(IntentKey.EMAIL, currentEmail)
            .putExtra(IntentKey.PASSWORD, currentPassword)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startResetPasswordActivity() {
        val intent = Intent().getResetPasswordIntent(this, email = currentEmail)
        startActivityForResult(intent, ActivityRequestCodes.RESET_FLOW)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
