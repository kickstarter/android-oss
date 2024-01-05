package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.libs.Logout
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.ChangePasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.ChangePasswordViewModel
import io.reactivex.disposables.CompositeDisposable

class ChangePasswordActivity : ComponentActivity() {

    private var logout: Logout? = null
    private lateinit var disposables: CompositeDisposable
    private var theme = AppThemes.MATCH_SYSTEM.ordinal
    private lateinit var viewModelFactory: ChangePasswordViewModel.Factory
    private val viewModel: ChangePasswordViewModel.ChangePasswordViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
        var darkModeEnabled = false

        this.getEnvironment()?.let { env ->
            viewModelFactory = ChangePasswordViewModel.Factory(env)

            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }
        setContent {
            var showProgressBar =
                viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var error = viewModel.outputs.error().subscribeAsState(initial = "").value

            var scaffoldState = rememberScaffoldState()

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
                ChangePasswordScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onAcceptButtonClicked = { current, new ->
                        viewModel.updatePasswordData(current, new)
                        viewModel.inputs.changePasswordClicked()
                    },
                    showProgressBar = showProgressBar,
                    scaffoldState = scaffoldState
                )
            }

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        scaffoldState.snackbarHostState.showSnackbar(error)
                        viewModel.resetError()
                    }
                }
            }
        }

        this.logout = getEnvironment()?.logout()

        this.viewModel.outputs.success()
            .compose(Transformers.observeForUIV2())
            .subscribe { logout(it) }
            .addToDisposable(disposables)
    }

    private fun logout(email: String) {
        this.logout?.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra(IntentKey.LOGIN_REASON, LoginReason.CHANGE_PASSWORD)
                .putExtra(IntentKey.EMAIL, email)
        )
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
