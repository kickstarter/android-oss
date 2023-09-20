package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.kickstarter.R
import com.kickstarter.libs.Logout
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.ChangePasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.ChangePasswordViewModel
import com.kickstarter.viewmodels.ChangePasswordViewModelFactory
import com.kickstarter.viewmodels.GENERIC_ERROR
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class ChangePasswordActivity : ComponentActivity() {

    private var logout: Logout? = null
    private lateinit var disposables: CompositeDisposable

    private lateinit var viewModelFactory: ChangePasswordViewModelFactory
    private val viewModel: ChangePasswordViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
        var darkModeEnabled = false

        this.getEnvironment()?.let { env ->
            viewModelFactory = ChangePasswordViewModelFactory(env)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
        }

        setContent {
            val showProgressBar =
                viewModel.isLoading.collectAsState(initial = false).value

            val error = viewModel.error.collectAsState(initial = "").value

            val scaffoldState = rememberScaffoldState()

            KickstarterApp(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
                ChangePasswordScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onAcceptButtonClicked = { current, new ->
                        viewModel.updatePassword(current, new)
                    },
                    showProgressBar = showProgressBar,
                    scaffoldState = scaffoldState
                )
            }

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(scaffoldState) {
                        if (error == GENERIC_ERROR) {
                            scaffoldState.snackbarHostState.showSnackbar(getString(R.string.signup_error_something_wrong))
                        } else {
                            scaffoldState.snackbarHostState.showSnackbar(error)
                        }
                        viewModel.resetError()
                    }
                }
            }
        }

        this.logout = getEnvironment()?.logout()

        lifecycleScope.launch {
            viewModel.success.collect { email ->
                if (email.isNotEmpty()) logout(email)
            }
        }
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
