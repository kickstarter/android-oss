package com.kickstarter.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.login.SetPasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.viewmodels.SetPasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SetPasswordActivity : AppCompatActivity() {
    private lateinit var viewModelFactory: SetPasswordViewModel.Factory
    private val viewModel: SetPasswordViewModel.SetPasswordViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()
    private var environment: Environment? = null
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var darkModeEnabled = false

        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = SetPasswordViewModel.Factory(env)
            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        setContent {
            var showProgressBar =
                viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var error = viewModel.outputs.error().subscribeAsState(initial = "").value

            var headline: String? by remember { mutableStateOf("") }

            viewModel.outputs.setUserEmail()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { headline = getEnvironment()?.ksString()?.format(getString(R.string.We_will_be_discontinuing_the_ability_to_log_in_via_FB), "email", it) }
                .addToDisposable(disposables)

            var scaffoldState = rememberScaffoldState()

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error)
                        viewModel.resetError()
                    }
                }
            }

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
                SetPasswordScreen(

                    onSaveButtonClicked = { newPassword ->
                        viewModel.inputs.newPassword(newPassword)
                        viewModel.inputs.confirmPassword(newPassword)
                        viewModel.inputs.savePasswordClicked()
                    },
                    showProgressBar = showProgressBar,
                    headline = headline,
                    isFormSubmitting = viewModel.outputs.isFormSubmitting().subscribeAsState(initial = false).value,
                    onTermsOfUseClicked = { startDisclaimerScreen(DisclaimerItems.TERMS) },
                    onPrivacyPolicyClicked = { startDisclaimerScreen(DisclaimerItems.PRIVACY) },
                    onCookiePolicyClicked = { startDisclaimerScreen(DisclaimerItems.COOKIES) },
                    onHelpClicked = { startDisclaimerScreen(DisclaimerItems.HELP) },
                    scaffoldState = scaffoldState
                )
            }
        }
        viewModel.configureWith(intent)

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finish() }
            .addToDisposable(disposables)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // do nothing
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
}
