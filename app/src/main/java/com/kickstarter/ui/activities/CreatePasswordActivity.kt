package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.libs.Environment
import com.kickstarter.libs.Logout
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.activities.compose.login.CreatePasswordScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.viewmodels.CreatePasswordViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: CreatePasswordViewModel.Factory
    private val viewModel: CreatePasswordViewModel.CreatePasswordViewModel by viewModels { viewModelFactory }
    private var logout: Logout? = null
    private lateinit var environment: Environment
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = CreatePasswordViewModel.Factory(env)
            environment = env
        }

        setContent {
            val showProgressBar =
                viewModel.outputs.progressBarIsVisible().subscribeAsState(initial = false).value

            val error = viewModel.outputs.error().subscribeAsState(initial = "").value

            val snackbarHostState = remember { SnackbarHostState() }

            val darModeEnabled = this.isDarkModeEnabled(env = environment)
            KickstarterApp(useDarkTheme = darModeEnabled) {
                CreatePasswordScreen(
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() },
                    onAcceptButtonClicked = { new ->
                        viewModel.updatePasswordData(new)
                        viewModel.createPasswordClicked()
                    },
                    showProgressBar = showProgressBar
                )
            }

            when {
                error.isNotEmpty() -> {
                    LaunchedEffect(snackbarHostState) {
                        snackbarHostState.showSnackbar(error)
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
