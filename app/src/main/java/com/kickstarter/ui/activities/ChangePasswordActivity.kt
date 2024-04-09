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
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.libs.Logout
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.ChangePasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.viewmodels.ChangePasswordViewModel
import com.kickstarter.viewmodels.ChangePasswordViewModelFactory
import io.reactivex.disposables.CompositeDisposable

class ChangePasswordActivity : ComponentActivity() {

    private var logout: Logout? = null
    private lateinit var disposables: CompositeDisposable
    private var theme = AppThemes.MATCH_SYSTEM.ordinal
    private lateinit var viewModelFactory: ChangePasswordViewModelFactory
    private val viewModel: ChangePasswordViewModel by viewModels {
        viewModelFactory
    }
    private var oAuthIsEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
        var darkModeEnabled = false

        this.getEnvironment()?.let { env ->
            viewModelFactory = ChangePasswordViewModelFactory(env)

            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal

            oAuthIsEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_OAUTH) ?: false
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val showProgressBar = uiState.isLoading
            val error = uiState.errorMessage
            val email = uiState.email

            val scaffoldState = rememberScaffoldState()

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
                        viewModel.updatePassword(current, new)
                    },
                    showProgressBar = showProgressBar,
                    scaffoldState = scaffoldState
                )
            }

            error?.let {
                LaunchedEffect(scaffoldState) {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                    viewModel.resetError()
                }
            }

            this.logout = getEnvironment()?.logout()

            email?.let {
                if (it.isNotEmpty()) logout(it)
            }
        }
    }

    private fun logout(email: String) {
        this.logout?.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
        val intent = Intent(this, LoginToutActivity::class.java)
        startActivity(
            intent
        )
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
