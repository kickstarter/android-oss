package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.libs.utils.extensions.isProjectSurveyUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.services.RequestHandler
import com.kickstarter.ui.compose.WebViewScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.SurveyResponseViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.rx2.asFlow
import okhttp3.Request

class SurveyResponseActivity : AppCompatActivity() {

    private lateinit var factory: SurveyResponseViewModel.Factory
    private val viewModel: SurveyResponseViewModel.ViewModel by viewModels { factory }
    private val disposables = CompositeDisposable()

    private val confirmationDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.Got_it_your_survey_response_has_been_submitted))
            .setPositiveButton(
                getString(R.string.general_alert_buttons_ok)
            ) { _: DialogInterface?, _: Int -> viewModel.inputs.okButtonClicked() }
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            intent?.let {
                factory = SurveyResponseViewModel.Factory(env, it)
            }
            setContent {
                KickstarterApp(
                    useDarkTheme = this.isDarkModeEnabled(env = env),
                ) {
                    val urlState by viewModel.webViewUrl().asFlow().collectAsStateWithLifecycle(initialValue = "")
                    WebViewScreen(
                        onBackButtonClicked = { finishWithAnimation() },
                        toolbarTitle = stringResource(id = R.string.Survey),
                        url = urlState,
                        isInDarkTheme = this.isDarkModeEnabled(env = env),
                        requestHandlers = listOf(
                            RequestHandler(
                                { uri: Uri, webEndpoint: String ->
                                    uri.isProjectSurveyUri(webEndpoint)
                                }) { request: Request, webView: WebView ->
                                handleProjectSurveyUriRequest(request, webView)
                            },
                            RequestHandler({ uri: Uri, webEndpoint: String ->
                                uri.isProjectUri(webEndpoint)
                            }) { request: Request, webView: WebView ->
                                handleProjectUriRequest(request, webView)
                            }
                        )
                    )
                }
            }
        }

        viewModel.outputs.goBack()
            .compose(Transformers.observeForUIV2())
            .subscribe { onBackPressedDispatcher.onBackPressed() }
            .addToDisposable(disposables)

        viewModel.outputs.showConfirmationDialog()
            .compose(Transformers.observeForUIV2())
            .subscribe { confirmationDialog.show() }
            .addToDisposable(disposables)
    }

    private fun handleProjectUriRequest(request: Request, webView: WebView): Boolean {
        viewModel.inputs.projectUriRequest(request)
        return true
    }

    private fun handleProjectSurveyUriRequest(request: Request, webView: WebView): Boolean {
        viewModel.inputs.projectSurveyUriRequest(request)
        return false
    }
}
