package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.libs.Logout
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.login.CreatePasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.viewmodels.CreatePasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: CreatePasswordViewModel.Factory
    private val viewModel: CreatePasswordViewModel.CreatePasswordViewModel by viewModels { viewModelFactory }
    private var logout: Logout? = null
    private val disposables = CompositeDisposable()
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var darkModeEnabled = false

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = CreatePasswordViewModel.Factory(env)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
            env
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
                CreatePasswordScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onAcceptButtonClicked = { new ->
                        viewModel.updatePasswordData(new)
                        viewModel.createPasswordClicked()
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

        this.logout = environment?.logout()

        this.viewModel.outputs.success()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logout(it) }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun logout(email: String) {
        this.logout?.execute()
        ApplicationUtils.startNewDiscoveryActivity(this)
        startActivity(
            Intent(this, LoginToutActivity::class.java)
        )
    }
}
