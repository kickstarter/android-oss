package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import com.kickstarter.libs.Logout
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.ChangePasswordScreen
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.ChangePasswordViewModel
import io.reactivex.disposables.CompositeDisposable

class ChangePasswordActivity : ComponentActivity() {

    private var logout: Logout? = null
    private lateinit var disposables: CompositeDisposable

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
        }
        setContent {
            var showProgressBar =
                viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var error = viewModel.outputs.error().subscribeAsState(initial = "").value

            var scaffoldState = rememberScaffoldState()

            KSTheme(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
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
