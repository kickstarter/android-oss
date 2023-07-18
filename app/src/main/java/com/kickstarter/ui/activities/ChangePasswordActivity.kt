package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import com.kickstarter.libs.Logout
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

        this.getEnvironment()?.let { env ->
            viewModelFactory = ChangePasswordViewModel.Factory(env)
        }
        setContent {
            // Replace with feature flag
            var darkMode by remember { mutableStateOf(false) }

            var showProgressBar = viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            var errorMessage by remember { mutableStateOf("") }

            KSTheme(useDarkTheme = darkMode) {
                ChangePasswordScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onAcceptButtonClicked = { current, new ->
                        viewModel.updatePasswordData(current, new)
                        viewModel.inputs.changePasswordClicked()
                    },
                    showProgressBar = showProgressBar,
                    errorMessage = errorMessage
                )
            }

            this.viewModel.outputs.error()
                .compose(Transformers.observeForUIV2())
                .subscribe {
                    errorMessage = it
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.resetError()
                    }, 2750)
                }
                .addToDisposable(disposables)
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
