package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.kickstarter.R
import com.kickstarter.databinding.SurveyResponseLayoutBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isProjectSurveyUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.services.RequestHandler
import com.kickstarter.viewmodels.SurveyResponseViewModel
import io.reactivex.disposables.CompositeDisposable
import okhttp3.Request
import java.util.Arrays

class SurveyResponseActivity : ComponentActivity() {

    private lateinit var binding: SurveyResponseLayoutBinding
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = SurveyResponseLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }

            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)

            WindowInsetsCompat.CONSUMED
        }

        getEnvironment()?.let { env ->
            factory = SurveyResponseViewModel.Factory(environment = env, intent = intent)
        }

        binding.surveyResponseWebView.registerRequestHandlers(
            Arrays.asList(
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

    override fun onResume() {
        super.onResume()
        viewModel.outputs.webViewUrl()
            .take(1)
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.surveyResponseWebView.loadUrl(it) }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
