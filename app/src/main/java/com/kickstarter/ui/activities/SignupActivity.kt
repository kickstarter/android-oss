package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.activities.compose.login.SignupScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.startDisclaimerActivity
import com.kickstarter.viewmodels.SignupViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SignupActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: SignupViewModel.Factory
    private val viewModel: SignupViewModel.SignupViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()
    var darkModeEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = SignupViewModel.Factory(env)
            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
        }

        setContent {
            var scaffoldState = rememberScaffoldState()

            var showProgressBar =
                    viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var error = viewModel.outputs.errorString().subscribeAsState(initial = "").value

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error)
                    }
                }
            }
            KickstarterApp(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
                SignupScreen(
                        onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                        onSignupButtonClicked = { name, email, password, sendNewsletters ->
                            viewModel.inputs.name(name)
                            viewModel.inputs.email(email)
                            viewModel.inputs.password(password)
                            viewModel.inputs.sendNewsletters(sendNewsletters)
                            viewModel.inputs.signupClick()
                        },
                        showProgressBar = showProgressBar,
                        isFormSubmitting = viewModel.outputs.formSubmitting().subscribeAsState(initial = false).value,
                        onTermsOfUseClicked = { startDisclaimerActivity(DisclaimerItems.TERMS) },
                        onPrivacyPolicyClicked = { startDisclaimerActivity(DisclaimerItems.PRIVACY) },
                        onCookiePolicyClicked = { startDisclaimerActivity(DisclaimerItems.COOKIES) },
                        onHelpClicked = { startDisclaimerActivity(DisclaimerItems.HELP) },
                        scaffoldState = scaffoldState
                )
            }
        }

        viewModel.outputs.signupSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onSuccess() }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        finish()
    }
}
